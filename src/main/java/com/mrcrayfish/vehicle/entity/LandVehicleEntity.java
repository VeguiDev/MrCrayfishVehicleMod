package com.mrcrayfish.vehicle.entity;

import com.mrcrayfish.vehicle.VehicleMod;
import com.mrcrayfish.vehicle.client.render.Wheel;
import com.mrcrayfish.vehicle.common.entity.PartPosition;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageDrift;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Author: MrCrayfish
 */
public abstract class LandVehicleEntity extends PoweredVehicleEntity
{
    private static final DataParameter<Boolean> DRIFTING = EntityDataManager.createKey(LandVehicleEntity.class, DataSerializers.BOOLEAN);

    public float drifting;
    public float additionalYaw;
    public float prevAdditionalYaw;

    public float frontWheelRotation;
    public float prevFrontWheelRotation;
    public float rearWheelRotation;
    public float prevRearWheelRotation;

    public LandVehicleEntity(EntityType<?> entityType, World worldIn)
    {
        super(entityType, worldIn);
    }

    @Override
    public void registerData()
    {
        super.registerData();
        this.dataManager.register(DRIFTING, false);
    }

    @Override
    public void onUpdateVehicle()
    {
        super.onUpdateVehicle();
        this.updateWheels();
    }

    @Override
    public void updateVehicle()
    {
        this.prevAdditionalYaw = this.additionalYaw;
        this.prevFrontWheelRotation = this.frontWheelRotation;
        this.prevRearWheelRotation = this.rearWheelRotation;
        this.updateDrifting();
    }

    @Override
    public void onClientUpdate()
    {
        super.onClientUpdate();
        LivingEntity entity = (LivingEntity) this.getControllingPassenger();
        if(entity != null && entity.equals(Minecraft.getInstance().player))
        {
            boolean drifting = VehicleMod.PROXY.isDrifting();
            if(this.isDrifting() != drifting)
            {
                this.setDrifting(drifting);
                PacketHandler.instance.sendToServer(new MessageDrift(drifting));
            }
        }
    }

    @Override
    public void updateVehicleMotion()
    {
        float currentSpeed = this.currentSpeed;

        if(this.speedMultiplier > 1.0F)
        {
            this.speedMultiplier = 1.0F;
        }

        /* Applies the speed multiplier to the current speed */
        currentSpeed = currentSpeed + (currentSpeed * this.speedMultiplier);

        VehicleProperties properties = this.getProperties();
        if(properties.getFrontAxelVec() != null && properties.getRearAxelVec() != null)
        {
            PartPosition bodyPosition = properties.getBodyPosition();
            Vec3d frontAxelVec = properties.getFrontAxelVec().scale(bodyPosition.getScale());
            frontAxelVec = frontAxelVec.scale(0.0625);
            Vec3d nextFrontAxelVec = new Vec3d(0, 0, currentSpeed / 20F).rotateYaw(this.wheelAngle * 0.017453292F);
            frontAxelVec = frontAxelVec.add(nextFrontAxelVec);
            Vec3d rearAxelVec = properties.getRearAxelVec().scale(bodyPosition.getScale());
            rearAxelVec = rearAxelVec.scale(0.0625);
            Vec3d nextRearAxelVec = new Vec3d(0, 0, currentSpeed / 20F);
            rearAxelVec = rearAxelVec.add(nextRearAxelVec);
            double angle = Math.atan2(rearAxelVec.z - frontAxelVec.z, rearAxelVec.x - frontAxelVec.x);
            angle = Math.toDegrees(angle) + 90.0;
            this.rotationYaw += angle;

            float f1 = MathHelper.sin(this.rotationYaw * 0.017453292F) / 20F; //Divide by 20 ticks
            float f2 = MathHelper.cos((this.rotationYaw) * 0.017453292F) / 20F;
            this.vehicleMotionX = (-currentSpeed * f1);
            if(!launching)
            {
                this.setMotion(this.getMotion().add(0, -0.08, 0));
            }
            this.vehicleMotionZ = (currentSpeed * f2);
        }
        else
        {
            float f1 = MathHelper.sin(this.rotationYaw * 0.017453292F) / 20F; //Divide by 20 ticks
            float f2 = MathHelper.cos(this.rotationYaw * 0.017453292F) / 20F;
            this.vehicleMotionX = (-currentSpeed * f1);
            if(!launching)
            {
                this.setMotion(this.getMotion().add(0, -0.08, 0));
            }
            this.vehicleMotionZ = (currentSpeed * f2);
        }
    }

    @Override
    protected void updateTurning()
    {
        this.turnAngle = VehicleMod.PROXY.getTargetTurnAngle(this, this.isDrifting());
        this.wheelAngle = this.turnAngle * Math.max(0.25F, 1.0F - Math.abs(currentSpeed / 30F));
        this.deltaYaw = this.wheelAngle * (currentSpeed / 30F) / 2F;

        if(world.isRemote)
        {
            this.targetWheelAngle = this.isDrifting() ? -35F * (this.turnAngle / (float) this.getMaxTurnAngle()) * this.getNormalSpeed() : this.wheelAngle - 35F * (this.turnAngle / (float) this.getMaxTurnAngle()) * drifting;
            this.renderWheelAngle = this.renderWheelAngle + (this.targetWheelAngle - this.renderWheelAngle) * (this.isDrifting() ? 0.35F : 0.5F);
        }
    }

    private void updateDrifting()
    {
        TurnDirection turnDirection = this.getTurnDirection();
        if(this.getControllingPassenger() != null && this.isDrifting())
        {
            if(turnDirection != TurnDirection.FORWARD)
            {
                AccelerationDirection acceleration = this.getAcceleration();
                if(acceleration == AccelerationDirection.FORWARD)
                {
                    this.currentSpeed *= 0.975F;
                }
                this.drifting = Math.min(1.0F, this.drifting + 0.025F);
            }
        }
        else
        {
            this.drifting *= 0.95F;
        }
        this.additionalYaw = 25F * this.drifting * (this.turnAngle / (float) this.getMaxTurnAngle()) * Math.min(this.getActualMaxSpeed(), this.getActualSpeed() * 2F);

        //Updates the delta yaw to consider drifting
        this.deltaYaw = this.wheelAngle * (this.currentSpeed / 30F) / (this.isDrifting() ? 1.5F : 2F);
    }

    public void updateWheels()
    {
        VehicleProperties properties = this.getProperties();
        double wheelCircumference = 16.0;
        double vehicleScale = properties.getBodyPosition().getScale();
        double speed = this.getSpeed();

        Wheel frontWheel = properties.getFirstFrontWheel();
        if(frontWheel != null)
        {
            double frontWheelCircumference = wheelCircumference * vehicleScale * frontWheel.getScaleY();
            double rotation = (speed * 16) / frontWheelCircumference;
            this.frontWheelRotation -= rotation * 20F;
        }

        Wheel rearWheel = properties.getFirstRearWheel();
        if(rearWheel != null)
        {
            double rearWheelCircumference = wheelCircumference * vehicleScale * rearWheel.getScaleY();
            double rotation = (speed * 16) / rearWheelCircumference;
            this.rearWheelRotation -= rotation * 20F;
        }
    }

    @Override
    public void createParticles()
    {
        if(this.canDrive())
        {
            super.createParticles();
        }
    }

    @Override
    protected void removePassenger(Entity passenger)
    {
        super.removePassenger(passenger);
        if(this.getControllingPassenger() == null)
        {
            this.rotationYaw -= this.additionalYaw;
            this.additionalYaw = 0;
            this.drifting = 0;
        }
    }

    @Override
    protected void applyYawToEntity(Entity passenger)
    {
        passenger.setRenderYawOffset(this.rotationYaw - this.additionalYaw);
        float f = MathHelper.wrapDegrees(passenger.rotationYaw - this.rotationYaw);
        float f1 = MathHelper.clamp(f, -120.0F, 120.0F);
        passenger.prevRotationYaw += f1 - f;
        passenger.rotationYaw += f1 - f;
        passenger.setRotationYawHead(passenger.rotationYaw);
    }

    public void setDrifting(boolean drifting)
    {
        this.dataManager.set(DRIFTING, drifting);
    }

    public boolean isDrifting()
    {
        return this.dataManager.get(DRIFTING);
    }

    @Override
    protected float getModifiedAccelerationSpeed()
    {
        if(trailer != null)
        {
            if(trailer.getPassengers().size() > 0)
            {
                return super.getModifiedAccelerationSpeed() * 0.5F;
            }
            else
            {
                return super.getModifiedAccelerationSpeed() * 0.8F;
            }
        }
        return super.getModifiedAccelerationSpeed();
    }

    @Override
    public float getModifiedRotationYaw()
    {
        return this.rotationYaw - this.additionalYaw;
    }
}
