package ssr

import (
	"context"
	"testing"

	"github.com/sagernet/sing-box/adapter/outbound"
	C "github.com/sagernet/sing-box/constant"
	"github.com/sagernet/sing-box/log"
	"github.com/sagernet/sing-box/option"
	"github.com/stretchr/testify/require"
)

func TestSSROutbound(t *testing.T) {
	registry := outbound.NewRegistry()
	RegisterOutbound(registry)

	// Verify that constant.TypeShadowsocksR is registered
	options := option.ShadowsocksROutboundOptions{
		ServerOptions: option.ServerOptions{
			Server:     "127.0.0.1",
			ServerPort: 14178,
		},
		Method:        "none",
		Password:      "xBVjvw",
		Protocol:      "auth_chain_a",
		ProtocolParam: "",
		Obfs:          "plain",
		ObfsParam:     "",
	}

	logger := log.NewNOPFactory().Logger()
	ob, err := NewOutbound(context.Background(), nil, logger, "test-ssr", options)
	require.NoError(t, err)
	require.NotNil(t, ob)
	require.Equal(t, C.TypeShadowsocksR, ob.Type())
	require.Equal(t, "test-ssr", ob.Tag())
	require.Equal(t, []string{"tcp"}, ob.Network())
}
