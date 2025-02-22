package com.sequenceiq.mock.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.sequenceiq.mock.swagger.model.ApiListBase;
import com.sequenceiq.mock.swagger.model.ApiMapEntryOfHostNameList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * A map from HDFS upgrade domains to host names, implemented as a list of ApiMapEntryOfHostNameList objects, where the keys are upgrade domain strings and the values are ApiHostNameList objects.
 */
@ApiModel(description = "A map from HDFS upgrade domains to host names, implemented as a list of ApiMapEntryOfHostNameList objects, where the keys are upgrade domain strings and the values are ApiHostNameList objects.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2021-12-10T21:24:30.629+01:00")




public class ApiHdfsUpgradeDomainList extends ApiListBase  {
  @JsonProperty("items")
  @Valid
  private List<ApiMapEntryOfHostNameList> items = null;

  public ApiHdfsUpgradeDomainList items(List<ApiMapEntryOfHostNameList> items) {
    this.items = items;
    return this;
  }

  public ApiHdfsUpgradeDomainList addItemsItem(ApiMapEntryOfHostNameList itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }
    this.items.add(itemsItem);
    return this;
  }

  /**
   * 
   * @return items
  **/
  @ApiModelProperty(value = "")

  @Valid

  public List<ApiMapEntryOfHostNameList> getItems() {
    return items;
  }

  public void setItems(List<ApiMapEntryOfHostNameList> items) {
    this.items = items;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiHdfsUpgradeDomainList apiHdfsUpgradeDomainList = (ApiHdfsUpgradeDomainList) o;
    return Objects.equals(this.items, apiHdfsUpgradeDomainList.items) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiHdfsUpgradeDomainList {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

