package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.tool;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class DataStructures {
    public static class RotationState {
        private float lastYaw;
        private float lastPitch;
        private float lastRoll;

        private double accumulatedYaw;
        private double accumulatedPitch;
        private double accumulatedRoll;

        public RotationState(LivingEntity entity) {
            this.init(entity);
        }

        public void init(LivingEntity entity) {
            this.lastYaw = entity.getYRot();
            this.lastPitch = entity.getXRot();
            this.lastRoll = 0.0F; // 默认无翻滚

            this.accumulatedYaw = 0D;
            this.accumulatedPitch = 0D;
            this.accumulatedRoll = 0D;
        }

        public void update(LivingEntity entity) {
            float newYaw = entity.getYRot();
            float newPitch = entity.getXRot();
            float newRoll = getEntityRoll(entity); // ← 兼容模组翻滚角

            // 计算三轴差值
            double yawDelta = calculateAngleDelta(lastYaw, newYaw);
            double pitchDelta = newPitch - lastPitch;
            double rollDelta = calculateAngleDelta(lastRoll, newRoll);

            // 三轴累计（单方向，反向归零）
            this.accumulatedYaw = calculateAccumulated(this.accumulatedYaw, yawDelta);
            this.accumulatedPitch = calculateAccumulated(this.accumulatedPitch, pitchDelta);
            this.accumulatedRoll = calculateAccumulated(this.accumulatedRoll, rollDelta);

            // 更新保存
            this.lastYaw = newYaw;
            this.lastPitch = newPitch;
            this.lastRoll = newRoll;
        }

        private double calculateAngleDelta(float last, float current) {
            double delta = current - last;
            delta = (delta + 180) % 360 - 180;
            return delta;
        }

        private double calculateAccumulated(double current, double delta) {
            if (Math.abs(delta) < 0.001) return current;
            if (Math.abs(current) < 0.001) return delta;
            if ((current > 0 && delta < 0) || (current < 0 && delta > 0)) return 0D;
            return current + delta;
        }

        private float getEntityRoll(LivingEntity entity) {
            // 优先从第三方MOD获取roll
            if (entity.getPersistentData().contains("Roll") && entity.getPersistentData().getFloat("Roll").isPresent()) {
                return entity.getPersistentData().getFloat("Roll").get();
            }
            // 无MOD则返回0
            return 0.0F;
        }

        @Override
        public String toString() {
            return "RotationState{" +
                    "lastYaw=" + lastYaw +
                    ", lastPitch=" + lastPitch +
                    ", lastRoll=" + lastRoll +
                    ", accumulatedYaw=" + accumulatedYaw +
                    ", accumulatedPitch=" + accumulatedPitch +
                    ", accumulatedRoll=" + accumulatedRoll +
                    '}';
        }

        public double getAccumulatedYaw() { return accumulatedYaw; }
        public double getAccumulatedPitch() { return accumulatedPitch; }
        public double getAccumulatedRoll() { return accumulatedRoll; }
    }

    public static class MovementState {
        private double lastWorldX;
        private double lastWorldY;
        private double lastWorldZ;

        private double accumulatedWorldX;
        private double accumulatedWorldY;
        private double accumulatedWorldZ;

        private double accumulatedRelativeX;
        private double accumulatedRelativeY;
        private double accumulatedRelativeZ;

        public MovementState(LivingEntity entity) {
            Vec3 pos = entity.position();
            this.init(pos);
        }

        public void init(Vec3 pos) {
            this.lastWorldX = pos.x();
            this.lastWorldY = pos.y();
            this.lastWorldZ = pos.z();

            this.accumulatedWorldX = 0D;
            this.accumulatedWorldY = 0D;
            this.accumulatedWorldZ = 0D;
            this.accumulatedRelativeX = 0D;
            this.accumulatedRelativeY = 0D;
            this.accumulatedRelativeZ = 0D;
        }

        public void update(LivingEntity entity) {
            Vec3 currentPos = entity.position();

            double dxWorld = currentPos.x() - lastWorldX;
            double dyWorld = currentPos.y() - lastWorldY;
            double dzWorld = currentPos.z() - lastWorldZ;

            double[] relativeDelta = this.worldToRelativeDelta(entity.getYRot(), dxWorld, dzWorld);
            double dxRel = relativeDelta[0];    // 相对X（右）
            double dyRel = dyWorld;             // 相对Y（上）= 世界Y
            double dzRel = relativeDelta[1];    // 相对Z（前）

            this.accumulatedWorldX = calculateAccumulated(this.accumulatedWorldX, dxWorld);
            this.accumulatedWorldY = calculateAccumulated(this.accumulatedWorldY, dyWorld);
            this.accumulatedWorldZ = calculateAccumulated(this.accumulatedWorldZ, dzWorld);

            this.accumulatedRelativeX = calculateAccumulated(this.accumulatedRelativeX, dxRel);
            this.accumulatedRelativeY = calculateAccumulated(this.accumulatedRelativeY, dyRel);
            this.accumulatedRelativeZ = calculateAccumulated(this.accumulatedRelativeZ, dzRel);

            this.lastWorldX = currentPos.x();
            this.lastWorldY = currentPos.y();
            this.lastWorldZ = currentPos.z();
        }

        /**
         * 世界位移 → 实体自身相对位移转换
         * @param yaw 实体偏航角
         * @param dxWorld 世界X轴位移
         * @param dzWorld 世界Z轴位移
         * @return [相对X(右), 相对Z(前)]
         */
        private double[] worldToRelativeDelta(float yaw, double dxWorld, double dzWorld) {
            double theta = Math.toRadians(yaw);
            double sin = Math.sin(theta);
            double cos = Math.cos(theta);

            double localX = dxWorld * cos + dzWorld * sin;   // +为左
            double localZ = -dxWorld * sin + dzWorld * cos;  // +为前
            return new double[]{localX, localZ};
        }

        private double calculateAccumulated(double current, double delta) {
            if (Math.abs(delta) < 0.001) return current;
            if (Math.abs(current) < 0.001) return delta;
            // 方向相反 → 立即归零
            if ((current > 0 && delta < 0) || (current < 0 && delta > 0)) return 0D;
            // 方向相同 → 累加
            return current + delta;
        }

        // ========== Getter ==========
        public double getAccumulatedWorldX() { return accumulatedWorldX; }
        public double getAccumulatedWorldY() { return accumulatedWorldY; }
        public double getAccumulatedWorldZ() { return accumulatedWorldZ; }

        public double getAccumulatedRelativeX() { return accumulatedRelativeX; }
        public double getAccumulatedRelativeY() { return accumulatedRelativeY; }
        public double getAccumulatedRelativeZ() { return accumulatedRelativeZ; }
    }
}

