package org.bluez;

import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.Variant;

import java.util.Map;

public interface GattManager1 extends DBusInterface {
	public void RegisterApplication(DBusInterface application, Map<String, Variant> options);
	public void UnregisterApplication(DBusInterface application);
}
