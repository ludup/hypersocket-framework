package com.hypersocket.netty;

import java.net.InetSocketAddress;

import com.hypersocket.netty.forwarding.NettyWebsocketClient;
import com.hypersocket.server.handlers.HttpResponseProcessor;
import com.hypersocket.server.handlers.WebsocketHandler;
import com.hypersocket.server.websocket.WebsocketClient;
import com.hypersocket.server.websocket.WebsocketClientCallback;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

class WebsocketConnectCallback implements WebsocketClientCallback {

	/**
	 * 
	 */
	Channel websocketChannel;
	HttpRequestServletWrapper request;
	HttpResponseServletWrapper response;
	WebsocketHandler handler;
	HttpResponseProcessor responseProcessor;
	
	public WebsocketConnectCallback(Channel websocketChannel,
			HttpRequestServletWrapper nettyRequest,
			HttpResponseServletWrapper nettyResponse,
			WebsocketHandler handler,
			HttpResponseProcessor responseProcessor) {
		this.websocketChannel = websocketChannel;
		this.request = nettyRequest;
		this.response = nettyResponse;
		this.handler = handler;
		this.responseProcessor = responseProcessor;
	}

	@Override
	public void websocketAccepted(final WebsocketClient client) {

		if (HttpRequestDispatcherHandler.log.isDebugEnabled()) {
			HttpRequestDispatcherHandler.log.debug("Socket connected, completing handshake for "
					+ websocketChannel.remoteAddress());
		}

		NettyWebsocketClient websocketClient = (NettyWebsocketClient) client;
		websocketClient.setWebsocketChannel(websocketChannel);

		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				HttpRequestDispatcherHandler.getWebSocketLocation(request.getNettyRequest()), "binary",
				true);

		WebSocketServerHandshaker handshaker = wsFactory
				.newHandshaker(request.getNettyRequest());
		if (handshaker == null) {
			WebSocketServerHandshakerFactory
					.sendUnsupportedVersionResponse(websocketChannel);
		} else {
			handshaker.handshake(websocketChannel,
					request.getNettyRequest()).addListener(
					new ChannelFutureListener() {

						@Override
						public void operationComplete(ChannelFuture future)
								throws Exception {

							if (future.isSuccess()) {
								if (HttpRequestDispatcherHandler.log.isDebugEnabled())
									HttpRequestDispatcherHandler.log.debug("Handshake complete for "
											+ websocketChannel
													.remoteAddress());
								client.open();
								websocketChannel.closeFuture().addListener(new ChannelFutureListener() {
									
									@Override
									public void operationComplete(ChannelFuture future) throws Exception {
										client.close();
									}
								});
							} else {
								if (HttpRequestDispatcherHandler.log.isDebugEnabled())
									HttpRequestDispatcherHandler.log.debug("Handshake failed for "
											+ websocketChannel
													.remoteAddress());
								client.close();
							}
						}
					});
		}
	}

	@Override
	public void websocketRejected(Throwable cause, int error) {
		response.setStatus(error);
		responseProcessor.sendResponse(request, response);
	}

	@Override
	public void websocketClosed(WebsocketClient client) {
		client.close();
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return new InetSocketAddress(request.getRemoteAddr(), request.getRemotePort());
	}

}