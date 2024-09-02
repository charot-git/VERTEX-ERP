package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.ComboBoxFilterUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocationComboBoxUtil {

    private final ComboBox<String> provinceComboBox;
    private final ComboBox<String> cityComboBox;
    private final ComboBox<String> barangayComboBox;

    private boolean isProvinceChanging = false;
    private boolean isCityChanging = false;

    public LocationComboBoxUtil(ComboBox<String> provinceComboBox, ComboBox<String> cityComboBox, ComboBox<String> barangayComboBox) {
        this.provinceComboBox = provinceComboBox;
        this.cityComboBox = cityComboBox;
        this.barangayComboBox = barangayComboBox;
    }

    public void initializeComboBoxes() {
        Map<String, String> provinceData = LocationCache.getProvinceData();
        Map<String, String> cityData = LocationCache.getCityData();
        Map<String, String> barangayData = LocationCache.getBarangayData();

        // Initialize Province ComboBox
        ObservableList<String> provinceItems = FXCollections.observableArrayList(provinceData.values());
        provinceComboBox.setItems(provinceItems);
        setupComboBoxFilter(provinceComboBox, provinceItems);

        // Initialize City ComboBox
        setupComboBoxFilter(cityComboBox, FXCollections.observableArrayList(cityData.values()));

        // Set up listeners
        setupProvinceChangeListener(provinceData, cityData, barangayData);
        setupCityChangeListener(cityData, barangayData, provinceData);
    }

    private void setupProvinceChangeListener(Map<String, String> provinceData, Map<String, String> cityData, Map<String, String> barangayData) {
        provinceComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !isProvinceChanging) {
                isProvinceChanging = true;

                String selectedProvinceCode = getKeyFromValue(provinceData, newValue);
                List<String> citiesInProvince = filterLocationsByParentCode(cityData, selectedProvinceCode);
                ObservableList<String> cityItems = FXCollections.observableArrayList(citiesInProvince);
                cityComboBox.setItems(cityItems);
                setupComboBoxFilter(cityComboBox, cityItems);

                // Maintain city selection if it is still valid
                String selectedCity = cityComboBox.getSelectionModel().getSelectedItem();
                if (selectedCity != null && citiesInProvince.contains(selectedCity)) {
                    cityComboBox.getSelectionModel().select(selectedCity);
                } else {
                    cityComboBox.getSelectionModel().clearSelection();
                }

                barangayComboBox.getItems().clear();
                isProvinceChanging = false;
            }
        });
    }

    private void setupCityChangeListener(Map<String, String> cityData, Map<String, String> barangayData, Map<String, String> provinceData) {
        cityComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !isCityChanging) {
                isCityChanging = true;

                String selectedCityCode = getKeyFromValue(cityData, newValue);

                // Update barangayComboBox
                List<String> barangaysInCity = filterLocationsByParentCode(barangayData, selectedCityCode);
                ObservableList<String> barangayItems = FXCollections.observableArrayList(barangaysInCity);
                barangayComboBox.setItems(barangayItems);
                setupComboBoxFilter(barangayComboBox, barangayItems);

                // Maintain province selection
                String provinceCode = getProvinceCodeFromCityCode(selectedCityCode);
                if (provinceCode != null) {
                    String provinceName = getValueFromKey(provinceData, provinceCode);
                    if (provinceName != null) {
                        provinceComboBox.getSelectionModel().select(provinceName);
                    }
                }

                isCityChanging = false;
            }
        });
    }

    private void setupComboBoxFilter(ComboBox<String> comboBox, ObservableList<String> items) {
        ComboBoxFilterUtil.setupComboBoxFilter(comboBox, items);
        comboBox.setTooltip(new Tooltip("Type here to filter"));
    }

    private String getKeyFromValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private List<String> filterLocationsByParentCode(Map<String, String> locationData, String parentCode) {
        List<String> filteredLocations = new ArrayList<>();
        for (Map.Entry<String, String> entry : locationData.entrySet()) {
            String code = entry.getKey();
            String parentCodeOfLocation = getParentCode(code);
            if (parentCodeOfLocation != null && parentCodeOfLocation.equals(parentCode)) {
                filteredLocations.add(entry.getValue());
            }
        }
        return filteredLocations;
    }

    private String getParentCode(String code) {
        if (code.length() == 9) {
            return code.substring(0, 6);
        } else if (code.length() == 6) {
            return code.substring(0, 4);
        } else {
            return null;
        }
    }

    private String getProvinceCodeFromCityCode(String cityCode) {
        if (cityCode.length() >= 4) {
            return cityCode.substring(0, 4);
        } else {
            return null;
        }
    }

    private String getValueFromKey(Map<String, String> dataMap, String key) {
        return dataMap.get(key);
    }
}
