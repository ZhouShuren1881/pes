package cn.edu.xmu.pes.controller;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BeanExceptionCollector {
    Logger logger = LoggerFactory.getLogger("BeanExceptionCollector");

    @Pointcut("execution(public * cn.edu.xmu.pes.controller.Major.*(..))")
    public void adaptor(){ }

    @Around("adaptor()")
    public void checkHttpkey(ProceedingJoinPoint joinPoint) {

        try {
            joinPoint.proceed();
        } catch (Throwable e) {
            logger.error(e.getMessage());
        }

    }

}
