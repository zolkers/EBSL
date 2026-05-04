package fr.riege.ebsl.common.layer;

public interface IPhysicsLayer {
    void setForward(float value);
    void setSideways(float value);
    void setJump(boolean value);
    void setSprint(boolean value);
    void setSneak(boolean value);
    void setYaw(float yaw);
    void setPitch(float pitch);
    void clearInputs();
}
