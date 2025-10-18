package sa.cerebra.task.sms.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import sa.cerebra.task.sms.SendSms;

@Service
@Log4j2
@Profile({"test","local"})
public class MockedSendSms implements SendSms {

    @Override
    public void send(String phone, String msg) {
       log.info("Sending SMS: {} -> to this phone {}", msg, phone);
    }
}
