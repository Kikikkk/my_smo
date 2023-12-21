package com.example;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AutoModeController {
        @FXML
        public Scene autoModePage;

        @FXML
        public TableColumn<DeviceInfo, Integer> deviceId;
        @FXML
        public TableColumn<DeviceInfo, Double> deviceLoadFactor;
        @FXML
        public TableView<DeviceInfo> deviceTable;

        @FXML
        public TableView<SourceInfo> srcTable;
        @FXML
        public TableColumn<SourceInfo, Integer> srcId;
        @FXML
        public TableColumn<SourceInfo, Integer> srcCount;
        @FXML
        public TableColumn<SourceInfo, Double> srcFailProb;
        @FXML
        public TableColumn<SourceInfo, Double> srcAverageTimeInSystem;
        @FXML
        public TableColumn<SourceInfo, Double> srcAverageTimeInBuffer;
        @FXML
        public TableColumn<SourceInfo, Double> srcAverageTimeInDevice;
        @FXML
        public TableColumn<SourceInfo, Double> srcDispersionBufferTime;
        @FXML
        public TableColumn<SourceInfo, Double> srcDispersionServiceTime;

        private final ObservableList<SourceInfo> sourcesInfo = FXCollections.observableArrayList();
        private final ObservableList<DeviceInfo> devicesInfo = FXCollections.observableArrayList();

        private static final int DEFAULT_DOUBLE_PLACES = 4;

        private static double round(double value) {
                long factor = (long) Math.pow(10, DEFAULT_DOUBLE_PLACES);
                value *= factor;
                long tmp = Math.round(value);
                return (double) tmp / factor;
        }

        public void sendStatistic(Statistic statistic) {
                sourcesInfo.addAll(statistic.getSourcesStat());
                devicesInfo.addAll(statistic.getDeviceStat());

                initSourceTable();
                initDeviceTable();
        }

        private void initSourceTable() {
                srcTable.setItems(sourcesInfo);
                srcId.setCellValueFactory(new PropertyValueFactory<>("id"));
                srcCount.setCellValueFactory(new PropertyValueFactory<>("requestCount"));
                configureDoubleColumn(srcFailProb,  SourceInfo::getFailureProb);
                configureDoubleColumn(srcAverageTimeInSystem, SourceInfo::getAvgTimeInSystem);
                configureDoubleColumn(srcAverageTimeInBuffer, SourceInfo::getAvgTimeInBuffer);
                configureDoubleColumn(srcAverageTimeInDevice, SourceInfo::getAvgTimeInDevice);
                configureDoubleColumn(srcDispersionBufferTime, SourceInfo::getDmBufferTime);
                configureDoubleColumn(srcDispersionServiceTime, SourceInfo::getDmServiceTime);
        }

        private void initDeviceTable() {
                deviceTable.setItems(devicesInfo);
                deviceId.setCellValueFactory(new PropertyValueFactory<>("id"));
                configureDoubleColumn(deviceLoadFactor, DeviceInfo::getLoadFactor);
        }

        private <T> void configureDoubleColumn(TableColumn<T, Double> column,
                        java.util.function.Function<T, Double> valueFunction) {
                column.setCellValueFactory(
                                dataCell -> new SimpleDoubleProperty(round(valueFunction.apply(dataCell.getValue())))
                                                .asObject());
        }
}
