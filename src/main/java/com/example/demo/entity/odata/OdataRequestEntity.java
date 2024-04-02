package com.example.demo.entity.odata;

import com.example.demo.entity.common.CommonEntity;
import lombok.Data;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;

import java.util.List;

@Data
public class OdataRequestEntity {
    private EdmEntitySet edmEntitySet;
    private EdmEntityType entityType;
    private List<CommonEntity.Join> join;
}
