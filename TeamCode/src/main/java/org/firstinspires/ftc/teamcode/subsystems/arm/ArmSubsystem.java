package org.firstinspires.ftc.teamcode.subsystems.arm;

import static com.arcrobotics.ftclib.util.MathUtils.clamp;
import static org.firstinspires.ftc.teamcode.helpers.utils.MotionProfile.FeedforwardType.COSINE;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.helpers.utils.GlobalConfig;
import org.firstinspires.ftc.teamcode.helpers.utils.MotionProfile;
import org.firstinspires.ftc.teamcode.helpers.subsystems.VLRSubsystem;

@Config
public class ArmSubsystem extends VLRSubsystem<ArmSubsystem> implements ArmConfiguration{
    private DcMotorEx motor;
    private DcMotorEx thoughBoreEncoder;

    private MotionProfile motionProfile;
    private SlideSubsystem slideSubsystem;


    protected void initialize(HardwareMap hardwareMap) {
        Telemetry telemetry = FtcDashboard.getInstance().getTelemetry();
        slideSubsystem = new SlideSubsystem(hardwareMap);

        motor = hardwareMap.get(DcMotorEx.class, MOTOR_NAME);
        thoughBoreEncoder = hardwareMap.get(DcMotorEx.class, ENCODER_NAME);
        motionProfile = new MotionProfile(telemetry, "ARM", ACCELERATION, DECELERATION, MAX_VELOCITY, FEEDBACK_PROPORTIONAL_GAIN, FEEDBACK_INTEGRAL_GAIN, FEEDBACK_DERIVATIVE_GAIN, VELOCITY_GAIN, ACCELERATION_GAIN, COSINE);
        motionProfile.enableTelemetry(true);
    }


    public void setTargetAngle(TargetAngle targetAngle){
        motionProfile.setCurrentTargetPosition(clamp(targetAngle.angleDegrees, MIN_ANGLE, MAX_ANGLE));
    }


    public void setTargetPosition(SlideConfiguration.TargetPosition targetPosition){
        slideSubsystem.setTargetPosition(targetPosition);
    }


    public void setTargetPosition(double targetPosition){
        slideSubsystem.setTargetPosition(targetPosition);
    }


    public double getAngleDegrees(){
        return thoughBoreEncoder.getCurrentPosition() / ENCODER_TICKS_PER_ROTATION * 360d;
    }


    @Override
    public void periodic(){
        motionProfile.updateCoefficients(ACCELERATION, DECELERATION, MAX_VELOCITY, FEEDBACK_PROPORTIONAL_GAIN, FEEDBACK_INTEGRAL_GAIN, FEEDBACK_DERIVATIVE_GAIN, VELOCITY_GAIN, ACCELERATION_GAIN);

        double currentAngle = getAngleDegrees();
        double currentFeedForwardGain = mapToRange(slideSubsystem.getPosition(), SlideSubsystem.MIN_POSITION, SlideSubsystem.MAX_POSITION, RETRACTED_FEEDFORWARD_GAIN, EXTENDED_FEEDFORWARD_GAIN);

        motionProfile.setFeedForwardGain(currentFeedForwardGain);

        double power = motionProfile.getPower(currentAngle);
        motor.setPower(power);

        slideSubsystem.periodic(currentAngle);
    }


    public static double mapToRange(double value, double minInput, double maxInput, double minOutput, double maxOutput){
        if (minInput == maxInput) {
            throw new IllegalArgumentException("inMIN and inMax cant be the same");
        }
        return minOutput + ((value - minInput) * (maxOutput - minOutput)) / (maxInput - minInput);
    }
}
