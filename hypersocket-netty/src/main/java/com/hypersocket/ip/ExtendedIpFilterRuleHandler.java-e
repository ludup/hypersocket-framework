package com.hypersocket.ip;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.ipfilter.IpFilterRule;
import org.jboss.netty.handler.ipfilter.IpFilterRuleHandler;
import org.springframework.stereotype.Component;

@Component
public class ExtendedIpFilterRuleHandler extends IpFilterRuleHandler {

	Set<ExtendedIpFilter> filters = new HashSet<ExtendedIpFilter>();
	
	boolean defaultRule = true;
	
	public ExtendedIpFilterRuleHandler() {
	}

	public ExtendedIpFilterRuleHandler(List<IpFilterRule> newList) {
		super(newList);
	}

	public void setDefaultRule(boolean defaultRule) {
		this.defaultRule = defaultRule;
	}
	
	public boolean getDefaultRule() {
		return defaultRule;
	}
	
	@Override
    protected boolean accept(ChannelHandlerContext ctx, ChannelEvent e, InetSocketAddress inetSocketAddress)
            throws Exception {
        if(!super.accept(ctx, e, inetSocketAddress)) {
        	return false;
        }
        
        for(ExtendedIpFilter filter : filters) {
        	if(filter.contains(inetSocketAddress)) {
        		return filter.accept(inetSocketAddress);
        	}
        }
        
        return defaultRule;
    }
}
