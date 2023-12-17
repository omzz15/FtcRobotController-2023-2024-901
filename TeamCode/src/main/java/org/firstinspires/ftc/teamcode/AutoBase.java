package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.teamcode.depricated.apriltags.Tag;
import org.firstinspires.ftc.teamcode.parts.drive.Drive;
import org.firstinspires.ftc.teamcode.parts.intake.Intake;
import org.firstinspires.ftc.teamcode.parts.positionsolver.PositionSolver;
import org.firstinspires.ftc.teamcode.parts.positionsolver.XRelativeSolver;
import org.firstinspires.ftc.teamcode.parts.positiontracker.PositionTracker;
import org.firstinspires.ftc.teamcode.parts.positiontracker.encodertracking.EncoderTracker;
import org.firstinspires.ftc.teamcode.parts.positiontracker.hardware.PositionTrackerHardware;
import org.firstinspires.ftc.teamcode.parts.positiontracker.settings.PositionTrackerSettings;
import org.firstinspires.ftc.teamcode.parts.teamprop.TeamProp;
import org.firstinspires.ftc.teamcode.parts.teamprop.TeamPropDetectionPipeline;

import java.text.DecimalFormat;
import java.util.function.Function;

import om.self.ezftc.core.Robot;
import om.self.ezftc.utils.Constants;
import om.self.ezftc.utils.Vector3;
import om.self.task.core.Group;
import om.self.task.core.TaskEx;
import om.self.task.other.TimedTask;

import static om.self.ezftc.utils.Constants.tileSide;

@Config
@Autonomous(name="AutoBase", group="Test")
public class AutoBase extends LinearOpMode{
    public Function<Vector3, Vector3> transformFunc;
    public boolean isRight;
    public Vector3 customStartPos;
    public boolean shutdownps;
    Intake intake;
    PositionSolver positionSolver;
    PositionTracker pt;
    EncoderTracker et;
    Tag aprilTag;
    TeamProp tp;

    //Vector3 centralspikemark = new Vector3(-35.25, -39.5, -90);
    Vector3 startPosition = new Vector3(-1.5,-2.7,90);

    public void initAuto(){
        transformFunc = (v) -> v;
        isRight = true;
    }

    private Vector3 tileToInchAuto(Vector3 tiles){
        return Constants.tileToInch(transformFunc.apply(tiles));
    }

    public Vector3 fieldToTile(Vector3 p){
        return new Vector3(p.X / tileSide, p.Y / tileSide, p.Z);
    }

    @Override
    public void runOpMode() {
        initAuto();
        FtcDashboard dashboard = FtcDashboard.getInstance();
        TelemetryPacket packet = new TelemetryPacket();
        Telemetry dashboardTelemetry = dashboard.getTelemetry();
        Robot r = new Robot(this);
        Drive d = new Drive(r);
        aprilTag = new Tag(r);
        tp = new TeamProp(r);

        PositionTrackerSettings pts = new PositionTrackerSettings(AxesOrder.XYZ, false, 100, new Vector3(2,2,2), tileToInchAuto(startPosition));
        //pts = pts.withPosition(customStartPos != null ? customStartPos : transformFunc.apply(pts.startPosition));
        pt = new PositionTracker(r, pts, PositionTrackerHardware.makeDefault(r));

        XRelativeSolver solver = new XRelativeSolver(d);
        EncoderTracker et = new EncoderTracker(pt);

        //intake = new Intake(r);
        //positionSolverLose = new PositionSolver(d,PositionSolverSettings.loseSettings);
        positionSolver = new PositionSolver(d);
        DecimalFormat df = new DecimalFormat("#0.0");
        r.init();

        while (!isStarted()) {
            aprilTag.onRun();
            telemetry.addLine(String.format("\nDetected tag ID=%s", aprilTag.detectedID));
            telemetry.addLine(String.format("\nPark ID=%s", aprilTag.parkID));
            r.opMode.telemetry.update();
            sleep(50);
        }

        r.start();
        if(shutdownps)
            positionSolver.triggerEvent(Robot.Events.STOP);

        Group container = new Group("container", r.taskManager);
        TimedTask autoTask = new TimedTask("auto task", container);
        positionSolver.setNewTarget(pt.getCurrentPosition(), true);

        autoTask.addStep(() -> {
            // init intake setup items here
        });
        // add calls to special autonomous action collections in methods below
        gotoTestPos(autoTask);

        while (opModeIsActive()) {
            telemetry.addData("Team Prop Position", tp.pipeline.position);
            double x = pt.getCurrentPosition().X;
            double y = pt.getCurrentPosition().Y;
            double z = Math.toRadians(pt.getCurrentPosition().Z);

            double x1 = Math.cos(z)*8;
            double y1 = Math.sin(z)*8;
            packet.fieldOverlay().setFill("blue").fillCircle(x,y,6);
            packet.fieldOverlay().setStroke("red").strokeLine(x,y,x+x1,y+y1);
            packet.fieldOverlay().fillCircle(1, 1, 1);

            telemetry.addData("pos id", pt.positionSourceId);

            r.run();
            //container.run();
            telemetry.addData("position", pt.getCurrentPosition());
            if(gamepad1.dpad_down) telemetry.addData("tasks", r.getTaskManager());
            if(gamepad1.dpad_down) telemetry.addData("events", r.getEventManager());
            telemetry.addData("tile position", fieldToTile(pt.getCurrentPosition()));

            dashboardTelemetry.update();
            telemetry.update();

        }
        r.stop();
    }
// ********* Put the methods that do the parts of the autonomous routines here ************//
    private void gotoTestPos(TaskEx autoTask) {
        //(-1.5,-2.6,0);
        Vector3 centralspikemark = new Vector3(-1.5, -1.69, 0);
        Vector3 pos1 = new Vector3(-1.5, 0, 90);
        Vector3 pos2 = new Vector3(-1.5, 0, 90);
        Vector3 pos3 = new Vector3(0.5,0,0);
        Vector3 centerAT = new Vector3(1.5,-1.5,180);
        Vector3 leftAT = new Vector3(1.5, -1, 180);
        Vector3 rightAT = new Vector3(1.5, -2, 180);
        positionSolver.addMoveToTaskEx(tileToInchAuto(centralspikemark), autoTask);
         positionSolver.addMoveToTaskEx(tileToInchAuto(pos1), autoTask);
         positionSolver.addMoveToTaskEx(tileToInchAuto(pos2), autoTask);
       positionSolver.addMoveToTaskEx(tileToInchAuto(pos3), autoTask);
        if(tp.pipeline.position == TeamPropDetectionPipeline.TeamPropPosition.CENTER)
            positionSolver.addMoveToTaskEx(tileToInchAuto(centerAT), autoTask);
        else if(tp.pipeline.position == TeamPropDetectionPipeline.TeamPropPosition.LEFT)
            positionSolver.addMoveToTaskEx(tileToInchAuto(leftAT), autoTask);
        else
            positionSolver.addMoveToTaskEx(tileToInchAuto(rightAT), autoTask);
    }
}
