package com.example.demo.service.adapter;

/**
 * 适配器，将 220V 电源转换为手机可用的 5V 电压。
 */
public class PowerAdapter implements PhonePower5V {

    /**
     * 被适配的高压电源。
     */
    private final HouseholdPower220V householdPower220V;

    /**
     * 构造适配器，注入被适配者。
     *
     * @param householdPower220V 家庭电源
     */
    public PowerAdapter(HouseholdPower220V householdPower220V) {
        this.householdPower220V = householdPower220V;
    }

    /**
     * 将 220V 转换为 5V。
     *
     * @return 适配后的 5V
     */
    @Override
    public int outputVoltage() {
        int inputVoltage = householdPower220V.outputVoltage();
        if (inputVoltage != 220) {
            throw new IllegalStateException("Only 220V input is supported.");
        }
        return 5;
    }
}
