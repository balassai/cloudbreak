package com.sequenceiq.cloudbreak.cloud.aws.common.consumption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import javax.validation.ValidationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.sequenceiq.cloudbreak.cloud.aws.common.connector.resource.AwsCloudWatchCommonService;
import com.sequenceiq.cloudbreak.cloud.exception.CloudConnectorException;
import com.sequenceiq.cloudbreak.cloud.model.CloudConsumption;
import com.sequenceiq.cloudbreak.cloud.model.Region;
import com.sequenceiq.cloudbreak.cloud.model.StorageSizeRequest;
import com.sequenceiq.cloudbreak.cloud.model.StorageSizeResponse;
import com.sequenceiq.common.model.FileSystemType;

@ExtendWith(MockitoExtension.class)
class AwsS3ConsumptionCalculatorTest {

    private static final String BUCKET_NAME = "mybucket";

    private static final String REGION_NAME = "bucket-location";

    private static final String ERROR_MESSAGE = "errormessage";

    private static final double DOUBLE_ASSERT_EPSILON = 0.001;

    private static final String S3_OBJECT_PATH = "s3a://bucket-name/folder/file";

    private static final String ABFS_OBJECT_PATH = "abfs://FILESYSTEM@STORAGEACCOUNT.dfs.core.windows.net/PATH";

    @Mock
    private AwsCloudWatchCommonService cloudWatchCommonService;

    @InjectMocks
    private AwsS3ConsumptionCalculator underTest;

    @Test
    public void getObjectStorageNoDatapointThrowsException() {
        Date startTime = Date.from(Instant.now().minus(42, ChronoUnit.MINUTES));
        Date endTime = Date.from(Instant.now());
        Region region = Region.region(REGION_NAME);
        StorageSizeRequest request = StorageSizeRequest.builder()
                .withObjectStoragePath(BUCKET_NAME)
                .withStartTime(startTime)
                .withEndTime(endTime)
                .withRegion(region)
                .build();

        GetMetricStatisticsResult statisticsResult = new GetMetricStatisticsResult()
                .withDatapoints(List.of());
        when(cloudWatchCommonService.getBucketSize(null, REGION_NAME, startTime, endTime, BUCKET_NAME)).thenReturn(statisticsResult);

        CloudConnectorException ex = assertThrows(CloudConnectorException.class, () -> underTest.calculate(request));

        verify(cloudWatchCommonService).getBucketSize(null, REGION_NAME, startTime, endTime, BUCKET_NAME);
        assertEquals(String.format("No datapoints were returned by CloudWatch for bucket %s and timeframe from %s to %s",
                BUCKET_NAME, startTime, endTime), ex.getMessage());
    }

    @Test
    public void getObjectStorageSizeOneDatapoint() {
        Date startTime = Date.from(Instant.now().minus(42, ChronoUnit.MINUTES));
        Date endTime = Date.from(Instant.now());
        Region region = Region.region(REGION_NAME);
        StorageSizeRequest request = StorageSizeRequest.builder()
                .withObjectStoragePath(BUCKET_NAME)
                .withStartTime(startTime)
                .withEndTime(endTime)
                .withRegion(region)
                .build();

        Datapoint datapoint = new Datapoint()
                .withTimestamp(Date.from(Instant.now()))
                .withMaximum(42.0);
        GetMetricStatisticsResult statisticsResult = new GetMetricStatisticsResult()
                .withDatapoints(List.of(datapoint));
        when(cloudWatchCommonService.getBucketSize(null, REGION_NAME, startTime, endTime, BUCKET_NAME)).thenReturn(statisticsResult);

        StorageSizeResponse result = underTest.calculate(request);

        verify(cloudWatchCommonService).getBucketSize(null, REGION_NAME, startTime, endTime, BUCKET_NAME);
        assertEquals(42.0, result.getStorageInBytes(), DOUBLE_ASSERT_EPSILON);
    }

    @Test
    public void getObjectStorageSizeLatestDatapointIsUsed() {
        Date startTime = Date.from(Instant.now().minus(42, ChronoUnit.MINUTES));
        Date endTime = Date.from(Instant.now());
        Region region = Region.region(REGION_NAME);
        StorageSizeRequest request = StorageSizeRequest.builder()
                .withObjectStoragePath(BUCKET_NAME)
                .withStartTime(startTime)
                .withEndTime(endTime)
                .withRegion(region)
                .build();

        Instant latestTimestamp = Instant.now();
        Datapoint latestDatapoint = new Datapoint()
                .withTimestamp(Date.from(latestTimestamp))
                .withMaximum(42.0);
        Datapoint earlierDatapoint = new Datapoint()
                .withTimestamp(Date.from(latestTimestamp.minus(1, ChronoUnit.DAYS)))
                .withMaximum(21.0);
        Datapoint earliestDatapoint = new Datapoint()
                .withTimestamp(Date.from(latestTimestamp.minus(2, ChronoUnit.DAYS)))
                .withMaximum(10.5);
        GetMetricStatisticsResult statisticsResult = new GetMetricStatisticsResult()
                .withDatapoints(List.of(latestDatapoint, earlierDatapoint, earliestDatapoint));
        when(cloudWatchCommonService.getBucketSize(null, REGION_NAME, startTime, endTime, BUCKET_NAME)).thenReturn(statisticsResult);

        StorageSizeResponse result = underTest.calculate(request);

        verify(cloudWatchCommonService).getBucketSize(null, REGION_NAME, startTime, endTime, BUCKET_NAME);
        assertEquals(42.0, result.getStorageInBytes(), DOUBLE_ASSERT_EPSILON);
    }

    @Test
    public void testGetBucketName() {
        assertEquals("bucket-name", underTest.getObjectId(S3_OBJECT_PATH));
    }

    @ParameterizedTest(name = "With requiredType={0} and storageLocation={1}, validation should succeed: {2}")
    @MethodSource("scenarios")
    public void testValidateCloudStorageType(FileSystemType requiredType, String storageLocation, boolean valid) {
        CloudConsumption cloudConsumption = CloudConsumption.builder().withStorageLocation(storageLocation).build();
        if (valid) {
            underTest.validate(cloudConsumption);
        } else {
            assertThrows(ValidationException.class, () -> underTest.validate(cloudConsumption));
        }
    }

    static Object[][] scenarios() {
        return new Object[][]{
                {FileSystemType.S3,     S3_OBJECT_PATH,     true},
                {FileSystemType.S3,     ABFS_OBJECT_PATH,   false},
                {FileSystemType.S3,     "",                 false},
                {FileSystemType.S3,     null,               false},
        };
    }
}