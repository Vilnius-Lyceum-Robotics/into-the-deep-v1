package org.firstinspires.ftc.teamcode.subsystems.arm.rotator;

import static com.arcrobotics.ftclib.util.MathUtils.clamp;
import static org.firstinspires.ftc.teamcode.helpers.utils.MotionProfile.FeedforwardType.COSINE;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.helpers.subsystems.VLRSubsystem;
import org.firstinspires.ftc.teamcode.helpers.utils.MotionProfile;
import org.firstinspires.ftc.teamcode.subsystems.arm.slide.ArmSlideConfiguration;
import org.firstinspires.ftc.teamcode.subsystems.arm.slide.ArmSlideSubsystem;

public class ArmRotatorSubsystem extends VLRSubsystem<ArmRotatorSubsystem> implements ArmRotatorConfiguration {
    private DcMotorEx motor;
    private DcMotorEx thoughBoreEncoder;

    private MotionProfile motionProfile;
    private ArmSlideSubsystem slideSubsystem;

    private RotatorState rotatorState;

    public static double mapToRange(double value, double minInput, double maxInput, double minOutput, double maxOutput) {
        if (minInput == maxInput) {
            throw new IllegalArgumentException("inMIN and inMax cant be the same");
        }
        return minOutput + ((value - minInput) * (maxOutput - minOutput)) / (maxInput - minInput);
    }

    protected void initialize(HardwareMap hardwareMap) {
        Telemetry telemetry = FtcDashboard.getInstance().getTelemetry();
        slideSubsystem = VLRSubsystem.getInstance(ArmSlideSubsystem.class);

        motor = hardwareMap.get(DcMotorEx.class, ArmRotatorConfiguration.MOTOR_NAME);
        motor.setDirection(DcMotorEx.Direction.REVERSE);

        thoughBoreEncoder = hardwareMap.get(DcMotorEx.class, ArmRotatorConfiguration.ENCODER_NAME);
        thoughBoreEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        thoughBoreEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        motionProfile = new MotionProfile(telemetry, "ARM", ArmRotatingPartConfiguration.ACCELERATION, ArmRotatingPartConfiguration.DECELERATION, ArmRotatingPartConfiguration.MAX_VELOCITY, ArmRotatingPartConfiguration.FEEDBACK_PROPORTIONAL_GAIN, ArmRotatingPartConfiguration.FEEDBACK_INTEGRAL_GAIN, ArmRotatingPartConfiguration.FEEDBACK_DERIVATIVE_GAIN, ArmRotatingPartConfiguration.VELOCITY_GAIN, ArmRotatingPartConfiguration.ACCELERATION_GAIN, COSINE);
        motionProfile.enableTelemetry(true);

        rotatorState = RotatorState.IN_ROBOT;
    }

    public void setTargetAngle(ArmRotatorConfiguration.TargetAngle targetAngle) {
        motionProfile.setCurrentTargetPosition(clamp(targetAngle.angleDegrees, ArmRotatorConfiguration.MIN_ANGLE, ArmRotatorConfiguration.MAX_ANGLE));
    }

    public void setTargetPosition(ArmSlideConfiguration.TargetPosition targetPosition) {
        slideSubsystem.setTargetPosition(targetPosition);
    }

    public void setTargetPosition(double targetPosition) {
        slideSubsystem.setTargetPosition(targetPosition);
    }

    public double getAngleDegrees() {
        return thoughBoreEncoder.getCurrentPosition() / ArmRotatorConfiguration.ENCODER_TICKS_PER_ROTATION * 360d;
    }

    @Override
    public void periodic() {
        motionProfile.updateCoefficients(ArmRotatorConfiguration.ACCELERATION, ArmRotatorConfiguration.DECELERATION, ArmRotatorConfiguration.MAX_VELOCITY, ArmRotatorConfiguration.FEEDBACK_PROPORTIONAL_GAIN, ArmRotatorConfiguration.FEEDBACK_INTEGRAL_GAIN, ArmRotatorConfiguration.FEEDBACK_DERIVATIVE_GAIN, ArmRotatorConfiguration.VELOCITY_GAIN, ArmRotatorConfiguration.ACCELERATION_GAIN);

        double currentAngle = getAngleDegrees();
        double currentFeedForwardGain = mapToRange(slideSubsystem.getPosition(), ArmSlideConfiguration.MIN_POSITION, ArmSlideConfiguration.MAX_POSITION, ArmRotatorConfiguration.RETRACTED_FEEDFORWARD_GAIN, ArmRotatorConfiguration.EXTENDED_FEEDFORWARD_GAIN);

        motionProfile.setFeedForwardGain(currentFeedForwardGain);

        double power = motionProfile.getPower(currentAngle);
        motor.setPower(power);

        slideSubsystem.periodic(currentAngle);
    }


    public RotatorState getRotatorState() {
        return this.rotatorState;
    }

    public void setRotatorState(RotatorState state) {
        this.rotatorState = state;
    }
}
