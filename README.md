New Found Gadget Proxy
======

This is really simple logging http proxy for use inside a container/pod.

Sole purpose of this proxy is for debugging a REST-like communication.
You just specify where to forward all traffic and this proxy will take care of forwarding all request, logging content and headers of requestsand replies.

Environmental variables
=======

- `TARGET` where to forward requests e.g. `http://example.com/baseurl`, special value `null` means no forwarding, just a logging black hole mode
