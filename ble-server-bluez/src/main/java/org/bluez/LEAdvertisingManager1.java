package org.bluez;

import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.Variant;

import java.util.Map;

public interface LEAdvertisingManager1 extends DBusInterface {
	public void RegisterAdvertisement(DBusInterface advertisement, Map<String, Variant> options);
	public void UnregisterAdvertisement(DBusInterface advertisement);
}
