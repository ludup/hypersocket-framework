package com.hypersocket.ip;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.ipfilter.IpFilteringHandlerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExtendedIpFilterRuleHandler extends IpFilteringHandlerImpl {
	
	@Autowired
	IPRestrictionService ipRestrictionService; 
	
	public ExtendedIpFilterRuleHandler() {
	}
	
	@Override
    protected boolean accept(ChannelHandlerContext ctx, ChannelEvent e, InetSocketAddress inetSocketAddress)
            throws Exception {		
		return ipRestrictionService.isAllowedAddress(inetSocketAddress.getAddress());
    }
}
