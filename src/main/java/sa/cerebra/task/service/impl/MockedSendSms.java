package sa.cerebra.task.service.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import sa.cerebra.task.service.SendSms;

@Service
@Log4j2
@Profile({"test,local"})
public class MockedSendSms implements SendSms {

    @Override
    public void send(String phone, String msg) {
       log.info("Sending SMS: {} -> to this phone {}", msg, phone);
    }
}
