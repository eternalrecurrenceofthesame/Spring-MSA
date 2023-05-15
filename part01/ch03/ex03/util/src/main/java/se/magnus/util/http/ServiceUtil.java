package se.magnus.util.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class ServiceUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceUtil.class);

    private final String port;

    private String serviceAddress = null;

    @Autowired
    public ServiceUtil(@Value("${server.port}") String port){ // 스프링 설정에서 각 마이크로 서비스의 포트를 가져온다.
        this.port = port;
    }

    public String getServiceAddress(){ // 마이크로 서비스 주소값을 찾는 메서드
        if(serviceAddress == null){
            serviceAddress = findMyHostname() + "/" + findMyIpAddress();
        }
        return serviceAddress;
    }

    private String findMyHostname(){
        try{
            return InetAddress.getLocalHost().getHostName(); //InetAddress 를 사용하면 아이피 주소를 가져올 수 있다.
        }catch(UnknownHostException e){
            return "unknown host name";
        }
    }

    private String findMyIpAddress(){
        try{
            return InetAddress.getLocalHost().getHostAddress();
        }catch(UnknownHostException e){
            return "unknown IP address";
        }
    }
}
