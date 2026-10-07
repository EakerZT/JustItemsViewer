These packet classes preserve the legacy payload IDs and codecs so older JIV
clients and servers remain compatible. Their Java types live in the
`eakerzt.jiv.common.network.packets.legacy` package to keep the compatibility code
separate from the current protocol.

New recipe-transfer protocol behavior belongs in separate packet types next to
this directory. Do not add fields to these legacy codecs.
