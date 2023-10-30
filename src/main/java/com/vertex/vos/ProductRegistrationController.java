package com.vertex.vos;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;

public class ProductRegistrationController {
    @FXML
    private TextField productCode;
    @FXML
    private TextField referenceCode;
    @FXML
    private TextField variantId;
    @FXML
    private TextField description;
    @FXML
    private TextField barCode;
    @FXML
    private TextField manufacturingCode;
    @FXML
    private TextField originalCost;
    @FXML
    private TextField FOBCost;
    @FXML
    private TextField landedCost;
    @FXML
    private TextField retailPrice;
    @FXML
    private TextField wholesalePrice;
    @FXML
    private TextField leastPrice;
    @FXML
    private TextField price4;
    @FXML
    private TextField price5;
    @FXML
    private TextField minimumQuantity;
    @FXML
    private TextField maximumQuantity;
    @FXML
    private TextField baseUnitCode1;
    @FXML
    private TextField baseUnitCode2;
    @FXML
    private TextField productStatus;
    @FXML
    private TextField requireSerialNo;
    @FXML
    private TextField divisionId;
    @FXML
    private TextField departmentId;
    @FXML
    private TextField sectionId;
    @FXML
    private TextField categoryId;
    @FXML
    private TextField categoryCode;
    @FXML
    private TextField brandId;
    @FXML
    private TextField colorId;
    @FXML
    private TextField promoId;
    @FXML
    private TextField supplierId;
    @FXML
    private TextField supplierItemIndex;
    @FXML
    private TextField kindCode;
    @FXML
    private TextField isAlias;
    @FXML
    private TextField includeInPromo;
    @FXML
    private TextField isAutoDelete;
    @FXML
    private TextField lastArrivalQuantity;
    @FXML
    private TextField lastDiscountText;
    @FXML
    private TextField lastArrivalDate;
    @FXML
    private TextField lastExpectedArrivalDate;
    @FXML
    private TextField remark;

    @FXML
    private TextField updatedBy;

    @FXML
    private TextField updatedDate;

    @FXML
    private TextField commission;

    @FXML
    private TextField compositionQuantity;

    @FXML
    private TextField lastFactorRate;

    @FXML
    private TextField designGroupId;

    @FXML
    private TextField designId;

    @FXML
    private TextField productLimitTypeId;

    @FXML
    private TextField productType;

    @FXML
    private TextField sizeRemark;

    @FXML
    private TextField matrixParentCode;

    @FXML
    private TextField matrixVariantGroupId;

    @FXML
    private TextField introductionDate;

    @FXML
    private TextField competitorSiteId;

    @FXML
    private TextField competitorRetailPrice;

    @FXML
    private TextField competitorPriceDate;

    @FXML
    private TextField packContentCode;

    @FXML
    private TextField packContentQuantity;

    @FXML
    private TextField isOrderingUnit;

    @FXML
    private TextField isInventoryUnit;

    @FXML
    private TextField length;

    @FXML
    private TextField width;

    @FXML
    private TextField height;

    @FXML
    private TextField weight;

    @FXML
    private TextField volume;

    @FXML
    private TextField isVATExempt;

    @FXML
    private TextField productLineId;

    @FXML
    private TextField unitId;

    @FXML
    private TextField VATType;

    @FXML
    private TextField shortName;

    @FXML
    private TextField referenceDate;

    @FXML
    private TextField masterId;

    @FXML
    private TextField isMarkDown;

    @FXML
    private TextField oldRetailPrice;

    @FXML
    private TextField oldName;

    @FXML
    private TextField retailPriceUpdateDate;

    @FXML
    private TextField nameUpdateDate;

    @FXML
    private TextField shelfCode;

    @FXML
    private TextField lastBarcode1;

    @FXML
    private TextField lastBarcode2;

    @FXML
    private TextField lastBarcode3;

    @FXML
    private TextField lastBarcode4;

    @FXML
    private TextField lastBarcode5;

    @FXML
    private TextField isRewardItem;

    @FXML
    private TextField requiredPoints;

    @FXML
    private TextField FOBCostWithoutVAT;

    @FXML
    private TextField seniorCitizenDiscountPercent;

    @FXML
    private TextField isWeighted;

    @FXML
    private TextField hsCode;

    @FXML
    private TextField requestServingParty;

    @FXML
    private TextField servingPartySiteList;

    @FXML
    private TextField supplierCommission;

    @FXML
    private TextField validSite;

    @FXML
    private TextField lastArrivalSupplier;

    @FXML
    private TextField disabledPersonDiscountPercent;

    @FXML
    private TextField isSeniorVATExempt;

    @FXML
    private TextField isSeniorWithNoLimit;

    @FXML
    private TextField additionalMarkupPercent;

    @FXML
    private TextField isEcoBag;

    @FXML
    private TextField requireItemRemark;

    @FXML
    private TextField isNoCLPPoints;

    @FXML
    private TextField isPWDVATExempt;

    @FXML
    private TextField isPWDWithNoLimit;

    @FXML
    private TextField isBestUnitBuy;

    @FXML
    private TextField isRequireSupervisor;

    @FXML
    private TextField onHoldAction;

    @FXML
    private TextField POSAlertMessage;

    @FXML
    private Button submitButton;

    @FXML
    private Tab require;
}
