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

import com.amazonaws.services.elasticfilesystem.model.DescribeFileSystemsResult;
import com.amazonaws.services.elasticfilesystem.model.FileSystemDescription;
import com.amazonaws.services.elasticfilesystem.model.FileSystemSize;
import com.sequenceiq.cloudbreak.cloud.aws.common.connector.resource.AwsEfsCommonService;
import com.sequenceiq.cloudbreak.cloud.exception.CloudConnectorException;
import com.sequenceiq.cloudbreak.cloud.model.CloudConsumption;
import com.sequenceiq.cloudbreak.cloud.model.Region;
import com.sequenceiq.cloudbreak.cloud.model.StorageSizeRequest;
import com.sequenceiq.cloudbreak.cloud.model.StorageSizeResponse;
import com.sequenceiq.common.model.FileSystemType;

@ExtendWith(MockitoExtension.class)
class AwsEFSConsumptionCalculatorTest {

    private static final String BUCKET_NAME = "fs-123345";

    private static final String REGION_NAME = "bucket-location";

    private static final String ERROR_MESSAGE = "errormessage";

    private static final double DOUBLE_ASSERT_EPSILON = 0.001;

    private static final String EFS_OBJECT_PATH = "fs-12312";

    private static final String ABFS_OBJECT_PATH = "abfs://FILESYSTEM@STORAGEACCOUNT.dfs.core.windows.net/PATH";

    @Mock
    private AwsEfsCommonService awsEfsCommonService;

    @InjectMocks
    private AwsEFSConsumptionCalculator underTest;

    @Test
    public void getEfsDoesNotExistThrowsException() {
        Date startTime = Date.from(Instant.now().minus(42, ChronoUnit.MINUTES));
        Date endTime = Date.from(Instant.now());
        Region region = Region.region(REGION_NAME);
        StorageSizeRequest request = StorageSizeRequest.builder()
                .withObjectStoragePath(BUCKET_NAME)
                .withStartTime(startTime)
                .withEndTime(endTime)
                .withRegion(region)
                .build();

        DescribeFileSystemsResult statisticsResult = new DescribeFileSystemsResult()
                .withFileSystems(List.of());
        when(awsEfsCommonService.getEfsSize(null, REGION_NAME, startTime, endTime, BUCKET_NAME)).thenReturn(statisticsResult);

        CloudConnectorException ex = assertThrows(CloudConnectorException.class, () -> underTest.calculate(request));

        verify(awsEfsCommonService).getEfsSize(null, REGION_NAME, startTime, endTime, BUCKET_NAME);
        assertEquals(String.format("No Efs were returned by efs id %s and timeframe from %s to %s",
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

        FileSystemDescription description = new FileSystemDescription()
                .withCreationTime(Date.from(Instant.now()))
                .withSizeInBytes(new FileSystemSize().withValue(42L));
        DescribeFileSystemsResult statisticsResult = new DescribeFileSystemsResult()
                .withFileSystems(List.of(description));
        when(awsEfsCommonService.getEfsSize(null, REGION_NAME, startTime, endTime, BUCKET_NAME)).thenReturn(statisticsResult);

        StorageSizeResponse result = underTest.calculate(request);

        verify(awsEfsCommonService).getEfsSize(null, REGION_NAME, startTime, endTime, BUCKET_NAME);
        assertEquals(42.0, result.getStorageInBytes(), DOUBLE_ASSERT_EPSILON);
    }

    @Test
    public void testEfsName() {
        assertEquals("fs-12312", underTest.getObjectId(EFS_OBJECT_PATH));
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
                {FileSystemType.EFS,     EFS_OBJECT_PATH,     true},
                {FileSystemType.EFS,     ABFS_OBJECT_PATH,   false},
                {FileSystemType.EFS,     "",                 false},
                {FileSystemType.EFS,     null,               false},
        };
    }
}