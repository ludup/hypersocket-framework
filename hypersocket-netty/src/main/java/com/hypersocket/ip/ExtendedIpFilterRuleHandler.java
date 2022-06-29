package com.hypersocket.ip;

import java.net.InetSocketAddress;

import javax.annotation.PostConstruct;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.ipfilter.IpFilteringHandlerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.netty.NettyServer;
import com.hypersocket.server.IPRestrictionConsumer;
import com.hypersocket.server.IPRestrictionService;

@Component
public class ExtendedIpFilterRuleHandler extends IpFilteringHandlerImpl {
	
	@Autowired
	private IPRestrictionService ipRestrictionService;
	
	@PostConstruct
	private void setup() {
		ipRestrictionService.registerService(new IPRestrictionConsumer(NettyServer.RESOURCE_BUNDLE, NettyServer.HTTPD));
	}
	
	public ExtendedIpFilterRuleHandler() {
	}
	
	@Override
    protected boolean accept(ChannelHandlerContext ctx, ChannelEvent e, InetSocketAddress inetSocketAddress)
            throws Exception {		
		return ipRestrictionService.isAllowedAddress(inetSocketAddress.getAddress(), NettyServer.HTTPD, null);
    }
}
