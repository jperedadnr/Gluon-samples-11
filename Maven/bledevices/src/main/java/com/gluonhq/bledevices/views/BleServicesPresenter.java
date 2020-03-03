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

import com.gluonhq.bledevices.BleDevices;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.attach.ble.BleDevice;
import com.gluonhq.attach.ble.BleProfile;
import com.gluonhq.attach.ble.BleSpecs;
import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.control.ListTile;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class BleServicesPresenter extends GluonPresenter<BleDevices> {

    @FXML
    private View bleServices;

    @FXML
    private CharmListView<BleProfile, String> serviceList;

    private BleDevice device;
    private ListChangeListener<BleProfile> bleProfileListChangeListener = change -> {
        while (change.next()) {
            System.out.println("profiles list change = " + change);
            if (device != null) {
                serviceList.setItems(device.getProfiles());
            }
        }
    };;

    public void initialize() {

        serviceList.setPlaceholder(new Label("No services found"));
        serviceList.setHeaderCellFactory(p -> new CharmListCell<>() {
            private final HBox box;
            {
                Label label = new Label(device.getName(), MaterialDesignIcon.BLUETOOTH_CONNECTED.graphic());
                box = new HBox(label);
                box.setAlignment(Pos.CENTER_LEFT);
            }
            @Override
            public void updateItem(BleProfile item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }

        });
        serviceList.setHeadersFunction(c -> device.getName());
        serviceList.setCellFactory(p -> new CharmListCell<>() {

            private BleProfile service;
            private final ListTile tile;
            {
                tile = new ListTile();
                tile.setPrimaryGraphic(MaterialDesignIcon.BLUETOOTH.graphic());
                tile.setOnMouseClicked(e -> {
                    AppViewManager.BLE_CHARS_VIEW.switchView().ifPresent(presenter ->
                            ((BleCharsPresenter) presenter).setService(device, service));
                });
            }

            @Override
            public void updateItem(BleProfile item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    service = item;
                    tile.setTextLine(0, BleSpecs.GattServices.ofAssignedNumber(BleSpecs.getAssignedNumber(item.getUuid())).getSpecificationName());
                    tile.setTextLine(1, String.format("UUID: 0x%04x", (int) BleSpecs.getAssignedNumber(item.getUuid())));
                    tile.setTextLine(2, item.getType());
                    setGraphic(tile);
                } else {
                    setGraphic(null);
                }

            }

        });

        bleServices.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = getApp().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.CHEVRON_LEFT.button(e ->
                        getApp().goHome()));
                appBar.setTitleText("Services");
            }
        });
    }

    void setDevice(BleDevice device) {
        this.device = device;
        ObservableList<BleProfile> profiles = device.getProfiles();
        profiles.addListener(bleProfileListChangeListener);
        serviceList.setItems(profiles);
        serviceList.disableProperty().bind(device.stateProperty().isNotEqualTo(BleDevice.State.STATE_CONNECTED));
    }

}
