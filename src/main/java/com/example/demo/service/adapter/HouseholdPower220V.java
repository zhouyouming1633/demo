package com.example.demo.service.adapter;

/**
 * 被适配者，代表家庭常见的 220V 电源。
 */
public class HouseholdPower220V {

    /**
     * 输出家庭电压。
     *
     * @return 固定的 220V
     */
    public int outputVoltage() {
        return 220;
    }
}
