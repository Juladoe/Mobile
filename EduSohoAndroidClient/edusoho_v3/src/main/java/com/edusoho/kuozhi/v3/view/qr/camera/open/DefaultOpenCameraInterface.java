package com.edusoho.kuozhi.v3.view.qr.camera.open;

import android.hardware.Camera;

/**
 * Default implementation for Android before API 9 / Gingerbread.
 */
final class DefaultOpenCameraInterface implements OpenCameraInterface {

	/**
	 * Calls {@link android.hardware.Camera#open()}.
	 */
	@Override
	public Camera open() {
		return Camera.open();
	}
}
