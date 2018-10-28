package larsoe4.morsetranslator;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;

/**
 * Created by larsoe4 on 10/23/2018.
 */

public class FlashlightMorseDevice implements MorseDevice {
    private CameraManager cameraManager = null;
    private String cameraId = null;


    public FlashlightMorseDevice(Context context) throws Exception{
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String[] cameraIds = cameraManager.getCameraIdList();

        for (String id : cameraIds){
            CameraCharacteristics cc = cameraManager.getCameraCharacteristics(id);
            if (cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)){
                cameraId = id;
                Log.d("resourceAvailability", "FOUND:CAMERA");
                return;
            }
        }

        // only reached if doesn't find a camera with flash and return
        throw new Exception();

    }

    @Override
    public void activate() {
        try {
            cameraManager.setTorchMode(cameraId, true);
        }catch (CameraAccessException e){
            Log.d("resourceAvailability", "UNABLE TO ACCESS CAMERA");
        }
    }

    @Override
    public void deactivate() {
        try {
            cameraManager.setTorchMode(cameraId, false);
        }catch (CameraAccessException e){
            Log.d("resourceAvailability", "UNABLE TO ACCESS CAMERA");
        }
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {
        deactivate();
    }
}
