package com.example.demo.entity.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ResponseEntity implements Serializable {
    private Integer ProductID;
    private String ProductName;
    private Integer SupplierID;
    private Integer CategoryID;
    private String QuantityPerUnit;
    private String UnitPrice;
    private Integer UnitsInStock;
    private Integer UnitsOnOrder;
    private Integer ReorderLevel;
    private Boolean Discontinued;

}
