package org.bluez;

import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.Variant;

import java.util.Map;

public interface GattCharacteristic1 extends DBusInterface {
	public byte[] ReadValue(Map<String, Variant> option);
	public void WriteValue(byte[] value, Map<String, Variant> option);
	public void StartNotify();
	public void StopNotify();
}
