/*
 * Copyright (c) 2020, Gluon
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of Gluon, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.bledevices.views;

import com.gluonhq.attach.ble.parser.BleParser;
import com.gluonhq.attach.ble.parser.BleSpecsFactory;
import com.gluonhq.bledevices.BleDevices;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.attach.ble.BleService;
import com.gluonhq.attach.ble.BleCharacteristic;
import com.gluonhq.attach.ble.BleDescriptor;
import com.gluonhq.attach.ble.BleDevice;
import com.gluonhq.attach.ble.BleProfile;
import com.gluonhq.attach.ble.BleSpecs;
import com.gluonhq.attach.ble.BleSpecs.GattCharacteristics;
import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.control.ListTile;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.control.TextField;

import java.util.Arrays;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class BleCharsPresenter extends GluonPresenter<BleDevices> {

    @FXML
    private View bleChars;

    @FXML
    private CharmListView<BleCharacteristic, String> charsList;

    private BleDevice device;
    private BleProfile service;

    public void initialize() {

        charsList.setPlaceholder(new Label("No characteristics found"));
        charsList.setHeaderCellFactory(p -> new CharmListCell<>() {
            private final HBox box;
            private final Label label;
            {
                final long assignedNumber = BleSpecs.getAssignedNumber(service.getUuid());
                String serviceName = BleSpecs.GattServices.ofAssignedNumber(assignedNumber).getSpecificationName();
                label = new Label(device.getName() + "::" + serviceName, MaterialDesignIcon.BLUETOOTH_CONNECTED.graphic());
                box = new HBox(label);
                box.setAlignment(Pos.CENTER_LEFT);
            }
            @Override
            public void updateItem(BleCharacteristic item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }

        });
        charsList.setHeadersFunction(c -> device.getName());
        charsList.setCellFactory(p -> new CharmListCell<>() {

            private BleCharacteristic characteristic;
            private final ListTile tile;
            private final ChangeListener<byte[]> valueListener;
            private final HBox hBox;
            private final Label readButton;
            private final Label writeButton;
            private final Label subscribeButton;
            private boolean subscribed;
            {
                tile = new ListTile();
                tile.setPrimaryGraphic(MaterialDesignIcon.BLUETOOTH.graphic());
                readButton = new Label("", MaterialDesignIcon.ARROW_DOWNWARD.graphic());
                readButton.setVisible(false);
                writeButton = new Label("", MaterialDesignIcon.ARROW_UPWARD.graphic());
                writeButton.setVisible(false);
                subscribeButton = new Label("", MaterialDesignIcon.REFRESH.graphic());
                subscribeButton.setVisible(false);
                readButton.managedProperty().bind(readButton.visibleProperty());
                writeButton.managedProperty().bind(writeButton.visibleProperty());
                subscribeButton.managedProperty().bind(subscribeButton.visibleProperty());
                hBox = new HBox(10, readButton, subscribeButton, writeButton);
                tile.setSecondaryGraphic(hBox);

                readButton.setOnMouseClicked(e -> {
                    BleService.create().ifPresent(ble -> ble.readCharacteristic(device, service.getUuid(), characteristic.getUuid()));
                });

                writeButton.setOnMouseClicked(e -> showDialog());

                subscribeButton.setOnMouseClicked(e -> {
                    if (!subscribed) {
                        BleService.create().ifPresent(ble -> {
                            ble.subscribeCharacteristic(device, service.getUuid(), characteristic.getUuid());
                            subscribed = true;
                            subscribeButton.setGraphic(MaterialDesignIcon.STOP.graphic());
                        });
                    } else {
                        BleService.create().ifPresent(ble -> {
                            ble.unsubscribeCharacteristic(device, service.getUuid(), characteristic.getUuid());
                            subscribed = false;
                            subscribeButton.setGraphic(MaterialDesignIcon.REFRESH.graphic());
                        });
                    }
                });
                valueListener = (obs, ov, nv) -> updateLine2();
            }

            @Override
            public void updateItem(BleCharacteristic item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    if (characteristic != null) {
                        characteristic.valueProperty().removeListener(valueListener);
                    }
                    characteristic = item;
                    characteristic.valueProperty().addListener(valueListener);
                    long assignedNumber = BleSpecs.getAssignedNumber(item.getUuid());
                    tile.setTextLine(0, BleSpecs.GattCharacteristics.ofAssignedNumber(assignedNumber).getSpecificationName());
                    tile.setTextLine(1, "UUID: 0x" + BleSpecs.formatToken(assignedNumber));

                    updateLine2();
                    readButton.setVisible(item.getProperties().contains("read"));
                    writeButton.setVisible(item.getProperties().contains("write"));
                    subscribeButton.setVisible(item.getProperties().contains("notify"));
                    setGraphic(tile);
                } else {
                    setGraphic(null);
                }

            }

            private void updateLine2() {
                if (characteristic == null) {
                    return;
                }
                String descriptors = "\nDescriptors:";
                for (BleDescriptor d : characteristic.getDescriptors()) {
                    d.valueProperty().removeListener(valueListener);
                    d.valueProperty().addListener(valueListener);
                    final long assignedNumber = BleSpecs.getAssignedNumber(d.getUuid());
                    final BleSpecs.GattDescriptors gattDescriptor = BleSpecs.GattDescriptors.ofAssignedNumber(assignedNumber);
                    String descriptorName = gattDescriptor.getSpecificationName();

                    final BleParser parser = BleSpecsFactory.getDescriptorParser(gattDescriptor);
                    descriptors = descriptors + "\n\t" + descriptorName +
                            "\n\t\tUUID: 0x" + BleSpecs.formatToken(assignedNumber) +
                            "\n\t\tValue: " + parser.parse(d.getValue());
                }
                String value = "\nValue: ";
                if (characteristic.getValue() != null) {
                    GattCharacteristics gattChar = GattCharacteristics.ofAssignedNumber(BleSpecs.getAssignedNumber(characteristic.getUuid()));
                    final BleParser parser = BleSpecsFactory.getCharacteristicsParser(gattChar);
                    value = value + parser.parse(characteristic.getValue());
                }
                tile.setTextLine(2, "Properties: " + characteristic.getProperties() + descriptors + value);
            }

            private void showDialog() {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitleText("BLE Write");
                final TextField textField = new TextField();
                textField.setFloatText("Byte String (comma separated, [-128 to 127])");
                dialog.setContent(textField);
                Button applyButton = new Button("Apply");
                applyButton.setDefaultButton(true);
                applyButton.setOnAction(event -> {
                    dialog.setResult(ButtonType.OK);
                    dialog.hide();
                });
                applyButton.disableProperty().bind(textField.textProperty().isEmpty());
                Button cancelButton = new Button("Cancel");
                cancelButton.setCancelButton(true);
                cancelButton.setOnAction(event -> {
                    dialog.setResult(ButtonType.CANCEL);
                    dialog.hide();
                });
                dialog.getButtons().addAll(applyButton, cancelButton);
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent()) {
                    ButtonType buttonType = result.get();
                    if (buttonType == ButtonType.OK) {
                        String[] byteValues = textField.getText().split(",");
                        byte[] bytes = new byte[byteValues.length];
                        for (int i=0, len=bytes.length; i<len; i++) {
                            try {
                                bytes[i] = Byte.parseByte(byteValues[i].trim());
                            } catch (NumberFormatException nfe) {
                                bytes[i] = 0;
                            }
                        }
                        BleService.create().ifPresent(ble -> ble.writeCharacteristic(device, service.getUuid(), characteristic.getUuid(), bytes));
                    }
                }
            }
        });

        bleChars.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = getApp().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.CHEVRON_LEFT.button(e ->
                        getApp().switchToPreviousView()));
                appBar.setTitleText("Characteristics");
            }
        });
    }

    void setService(BleDevice device, BleProfile service) {
        this.device = device;
        this.service = service;
        charsList.setItems(service.getCharacteristics());
        charsList.disableProperty().bind(device.stateProperty().isNotEqualTo(BleDevice.State.STATE_CONNECTED));
    }

}
