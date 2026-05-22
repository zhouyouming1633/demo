package com.example.demo.service.adapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 适配器模式测试类。
 */
class AdapterPatternTest {

    /**
     * 验证适配器可以将 220V 转换为 5V。
     */
    @Test
    void should_convert_220v_to_5v() {
        HouseholdPower220V householdPower220V = new HouseholdPower220V();
        PowerAdapter powerAdapter = new PowerAdapter(householdPower220V);

        int voltage = powerAdapter.outputVoltage();

        assertEquals(5, voltage);
    }

    /**
     * 验证客户端可以通过适配器完成充电。
     */
    @Test
    void should_charge_phone_via_adapter() {
        HouseholdPower220V householdPower220V = new HouseholdPower220V();
        PowerAdapter powerAdapter = new PowerAdapter(householdPower220V);
        PhoneChargingService chargingService = new PhoneChargingService();

        String chargeResult = chargingService.charge(powerAdapter);

        assertEquals("Phone is charging with 5V", chargeResult);
    }

    /**
     * 验证当输入电压不符合预期时，适配器会抛出异常。
     */
    @Test
    void should_throw_exception_when_voltage_is_invalid() {
        HouseholdPower220V invalidPower = new HouseholdPower220V() {
            /**
             * 覆盖父类输出，模拟异常输入电压。
             *
             * @return 非 220V 的电压
             */
            @Override
            public int outputVoltage() {
                return 110;
            }
        };
        PowerAdapter powerAdapter = new PowerAdapter(invalidPower);

        assertThrows(IllegalStateException.class, powerAdapter::outputVoltage);
    }
}
