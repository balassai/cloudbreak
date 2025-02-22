/**
 * NOTE: This class is auto generated by the swagger code generator program (2.4.16).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package com.sequenceiq.mock.swagger.v45.api;

import com.sequenceiq.mock.swagger.model.ApiMetricSchemaList;
import com.sequenceiq.mock.swagger.model.ApiTimeSeriesEntityAttributeList;
import com.sequenceiq.mock.swagger.model.ApiTimeSeriesEntityTypeList;
import com.sequenceiq.mock.swagger.model.ApiTimeSeriesRequest;
import com.sequenceiq.mock.swagger.model.ApiTimeSeriesResponseList;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2021-12-10T21:24:30.629+01:00")

@Api(value = "TimeSeriesResource", description = "the TimeSeriesResource API")
@RequestMapping(value = "/{mockUuid}/api/v45")
public interface TimeSeriesResourceApi {

    Logger log = LoggerFactory.getLogger(TimeSeriesResourceApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @ApiOperation(value = "Retrieve all metric entity type attributes monitored by Cloudera Manager.", nickname = "getEntityTypeAttributes", notes = "Retrieve all metric entity type attributes monitored by Cloudera Manager. <p/> Available since API v11.", response = ApiTimeSeriesEntityAttributeList.class, authorizations = {
        @Authorization(value = "basic")
    }, tags={ "TimeSeriesResource", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of time series entity attributes.", response = ApiTimeSeriesEntityAttributeList.class) })
    @RequestMapping(value = "/timeseries/entityTypeAttributes",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<ApiTimeSeriesEntityAttributeList> getEntityTypeAttributes(@ApiParam(value = "The unique id of CB cluster (works in CB test framework only)",required=true) @PathVariable("mockUuid") String mockUuid) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"items\" : [ {    \"name\" : \"...\",    \"displayName\" : \"...\",    \"description\" : \"...\",    \"isValueCaseSensitive\" : true  }, {    \"name\" : \"...\",    \"displayName\" : \"...\",    \"description\" : \"...\",    \"isValueCaseSensitive\" : true  } ]}", ApiTimeSeriesEntityAttributeList.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default TimeSeriesResourceApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Retrieve all metric entity types monitored by Cloudera Manager.", nickname = "getEntityTypes", notes = "Retrieve all metric entity types monitored by Cloudera Manager. It is guaranteed that parent types appear before their children. <p/> Available since API v11.", response = ApiTimeSeriesEntityTypeList.class, authorizations = {
        @Authorization(value = "basic")
    }, tags={ "TimeSeriesResource", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of time series entity type.", response = ApiTimeSeriesEntityTypeList.class) })
    @RequestMapping(value = "/timeseries/entityTypes",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<ApiTimeSeriesEntityTypeList> getEntityTypes(@ApiParam(value = "The unique id of CB cluster (works in CB test framework only)",required=true) @PathVariable("mockUuid") String mockUuid) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"items\" : [ {    \"name\" : \"...\",    \"category\" : \"...\",    \"nameForCrossEntityAggregateMetrics\" : \"...\",    \"displayName\" : \"...\",    \"description\" : \"...\",    \"immutableAttributeNames\" : [ \"...\", \"...\" ],    \"mutableAttributeNames\" : [ \"...\", \"...\" ],    \"entityNameFormat\" : [ \"...\", \"...\" ],    \"entityDisplayNameFormat\" : \"...\",    \"parentMetricEntityTypeNames\" : [ \"...\", \"...\" ]  }, {    \"name\" : \"...\",    \"category\" : \"...\",    \"nameForCrossEntityAggregateMetrics\" : \"...\",    \"displayName\" : \"...\",    \"description\" : \"...\",    \"immutableAttributeNames\" : [ \"...\", \"...\" ],    \"mutableAttributeNames\" : [ \"...\", \"...\" ],    \"entityNameFormat\" : [ \"...\", \"...\" ],    \"entityDisplayNameFormat\" : \"...\",    \"parentMetricEntityTypeNames\" : [ \"...\", \"...\" ]  } ]}", ApiTimeSeriesEntityTypeList.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default TimeSeriesResourceApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Retrieve schema for all metrics.", nickname = "getMetricSchema", notes = "Retrieve schema for all metrics <p/> The schema is fixed for a product version. The schema may change for an API versions <p/> Available since API v4.", response = ApiMetricSchemaList.class, authorizations = {
        @Authorization(value = "basic")
    }, tags={ "TimeSeriesResource", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of metric schema.", response = ApiMetricSchemaList.class) })
    @RequestMapping(value = "/timeseries/schema",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<ApiMetricSchemaList> getMetricSchema(@ApiParam(value = "The unique id of CB cluster (works in CB test framework only)",required=true) @PathVariable("mockUuid") String mockUuid) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"items\" : [ {    \"name\" : \"...\",    \"displayName\" : \"...\",    \"description\" : \"...\",    \"isCounter\" : true,    \"unitNumerator\" : \"...\",    \"unitDenominator\" : \"...\",    \"aliases\" : [ \"...\", \"...\" ],    \"sources\" : {      \"property1\" : [ \"...\", \"...\" ],      \"property2\" : [ \"...\", \"...\" ]    }  }, {    \"name\" : \"...\",    \"displayName\" : \"...\",    \"description\" : \"...\",    \"isCounter\" : true,    \"unitNumerator\" : \"...\",    \"unitDenominator\" : \"...\",    \"aliases\" : [ \"...\", \"...\" ],    \"sources\" : {      \"property1\" : [ \"...\", \"...\" ],      \"property2\" : [ \"...\", \"...\" ]    }  } ]}", ApiMetricSchemaList.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default TimeSeriesResourceApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Retrieve time-series data from the Cloudera Manager (CM) time-series data store using a tsquery.", nickname = "queryTimeSeries", notes = "Retrieve time-series data from the Cloudera Manager (CM) time-series data store using a tsquery.  Please see the <a href=\"https://docs.cloudera.com/r/cm_tsquery\"> tsquery language documentation</a>. <p/> Available since API v6.", response = ApiTimeSeriesResponseList.class, authorizations = {
        @Authorization(value = "basic")
    }, tags={ "TimeSeriesResource", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of time series that match the tsquery.", response = ApiTimeSeriesResponseList.class) })
    @RequestMapping(value = "/timeseries",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<ApiTimeSeriesResponseList> queryTimeSeries(@ApiParam(value = "The unique id of CB cluster (works in CB test framework only)",required=true) @PathVariable("mockUuid") String mockUuid,@ApiParam(value = "to return the response in. The content types \"application/json\" and \"text/csv\" are supported. This defaults to \"application/json\". If \"text/csv\" is specified then we return one row per time series data point, and we don't return any of the metadata.", defaultValue = "application/json") @Valid @RequestParam(value = "contentType", required = false, defaultValue="application/json") String contentType,@ApiParam(value = "Aggregate rollup level desired for the response data. Valid values are RAW, TEN_MINUTELY, HOURLY, SIX_HOURLY, DAILY, and WEEKLY. Note that if the mustUseDesiredRollup parameter is not set, then the monitoring server can decide to return a different rollup level.", defaultValue = "RAW") @Valid @RequestParam(value = "desiredRollup", required = false, defaultValue="RAW") String desiredRollup,@ApiParam(value = "Start of the period to query in ISO 8601 format (defaults to 5 minutes before the end of the period).") @Valid @RequestParam(value = "from", required = false) String from,@ApiParam(value = "If set then the tsquery will return data with the desired aggregate rollup level.", defaultValue = "false") @Valid @RequestParam(value = "mustUseDesiredRollup", required = false, defaultValue="false") Boolean mustUseDesiredRollup,@ApiParam(value = "Tsquery to run against the CM time-series data store.") @Valid @RequestParam(value = "query", required = false) String query,@ApiParam(value = "End of the period to query in ISO 8601 format (defaults to current time).", defaultValue = "now") @Valid @RequestParam(value = "to", required = false, defaultValue="now") String to) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"items\" : [ {    \"timeSeries\" : [ {      \"metadata\" : { },      \"data\" : [ { }, { } ]    }, {      \"metadata\" : { },      \"data\" : [ { }, { } ]    } ],    \"warnings\" : [ \"...\", \"...\" ],    \"timeSeriesQuery\" : \"...\"  }, {    \"timeSeries\" : [ {      \"metadata\" : { },      \"data\" : [ { }, { } ]    }, {      \"metadata\" : { },      \"data\" : [ { }, { } ]    } ],    \"warnings\" : [ \"...\", \"...\" ],    \"timeSeriesQuery\" : \"...\"  } ]}", ApiTimeSeriesResponseList.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default TimeSeriesResourceApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Retrieve time-series data from the Cloudera Manager (CM) time-series data store accepting HTTP POST request.", nickname = "queryTimeSeries_0", notes = "Retrieve time-series data from the Cloudera Manager (CM) time-series data store accepting HTTP POST request. This method differs from queryTimeSeries() in v6 that this could accept query strings that are longer than HTTP GET request limit.  Available since API v11.", response = ApiTimeSeriesResponseList.class, authorizations = {
        @Authorization(value = "basic")
    }, tags={ "TimeSeriesResource", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "List of time series that match the tsquery.", response = ApiTimeSeriesResponseList.class) })
    @RequestMapping(value = "/timeseries",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    default ResponseEntity<ApiTimeSeriesResponseList> queryTimeSeries_0(@ApiParam(value = "The unique id of CB cluster (works in CB test framework only)",required=true) @PathVariable("mockUuid") String mockUuid,@ApiParam(value = "Request object containing information used when retrieving timeseries data."  )  @Valid @RequestBody ApiTimeSeriesRequest body) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"items\" : [ {    \"timeSeries\" : [ {      \"metadata\" : { },      \"data\" : [ { }, { } ]    }, {      \"metadata\" : { },      \"data\" : [ { }, { } ]    } ],    \"warnings\" : [ \"...\", \"...\" ],    \"timeSeriesQuery\" : \"...\"  }, {    \"timeSeries\" : [ {      \"metadata\" : { },      \"data\" : [ { }, { } ]    }, {      \"metadata\" : { },      \"data\" : [ { }, { } ]    } ],    \"warnings\" : [ \"...\", \"...\" ],    \"timeSeriesQuery\" : \"...\"  } ]}", ApiTimeSeriesResponseList.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default TimeSeriesResourceApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
