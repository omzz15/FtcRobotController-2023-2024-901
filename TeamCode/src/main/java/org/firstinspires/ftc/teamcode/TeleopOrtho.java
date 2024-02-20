package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.parts.drive.DriveTeleop;
import org.firstinspires.ftc.teamcode.parts.drive.settings.DriveTeleopSettings;

@TeleOp(name="3 Teleop Ortho", group="Linear Opmode")
public class TeleopOrtho extends TestPixel {
    @Override
    public void initTeleop(){
        new DriveTeleop(drive, DriveTeleopSettings.makeOrtho(robot));
    }
}
