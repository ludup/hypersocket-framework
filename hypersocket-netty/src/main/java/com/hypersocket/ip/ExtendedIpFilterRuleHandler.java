package com.hypersocket.ip;

import java.net.InetSocketAddress;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.netty.NettyServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.ipfilter.AbstractRemoteAddressFilter;

@Component
@Sharable
public class ExtendedIpFilterRuleHandler extends AbstractRemoteAddressFilter<InetSocketAddress> {

	@Autowired
	private IPRestrictionService ipRestrictionService;

	@PostConstruct
	private void setup() {
		ipRestrictionService.registerService(new IPRestrictionConsumer(NettyServer.RESOURCE_BUNDLE, NettyServer.HTTPD));
	}

	public ExtendedIpFilterRuleHandler() {
	}

	@Override
	protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
		return ipRestrictionService.isAllowedAddress(remoteAddress.getAddress().getHostAddress(), NettyServer.HTTPD,
				null);
	}
}
