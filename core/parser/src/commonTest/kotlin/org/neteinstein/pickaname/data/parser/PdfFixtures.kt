package org.neteinstein.pickaname.data.parser

/**
 * A 2x2 "GÉNERO NOME" table shaped like the real source document: two "<gender> <name>" cells per
 * visual row, laid out side by side. Built by hand rather than sliced out of the real 2.9 MB PDF
 * so it can live inline, and shared by the platform extractor tests (each platform's actual uses
 * a completely different PDF engine, so each needs its own test against the same document).
 */
internal object PdfFixtures {
    const val TWO_COLUMN_PDF_BASE64 =
            "JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCjIgMCBvYmoKPD" +
            "wgL1R5cGUgL1BhZ2VzIC9LaWRzIFszIDAgUl0gL0NvdW50IDEgPj4KZW5kb2JqCjMgMCBvYmoKPDwgL1R5cGUgL1Bh" +
            "Z2UgL1BhcmVudCAyIDAgUiAvTWVkaWFCb3ggWzAgMCA0MjAgMjAwXSAvQ29udGVudHMgNCAwIFIgL1Jlc291cmNlcy" +
            "A8PCAvRm9udCA8PCAvRjEgNSAwIFIgPj4gPj4gPj4KZW5kb2JqCjQgMCBvYmoKPDwgL0xlbmd0aCAxNDIgPj4Kc3Ry" +
            "ZWFtCkJUCi9GMSAxMSBUZgo0MCAxNjAgVGQgKEZlbWluaW5vcyBBbmEpIFRqCjIwMCAwIFRkIChNYXNjdWxpbm9zIE" +
            "JydW5vKSBUagotMjAwIC0yMCBUZCAoRmVtaW5pbm9zIEJlYXRyaXopIFRqCjIwMCAwIFRkIChNYXNjdWxpbm9zIENh" +
            "cmxvcykgVGoKRVQKZW5kc3RyZWFtCmVuZG9iago1IDAgb2JqCjw8IC9UeXBlIC9Gb250IC9TdWJ0eXBlIC9UeXBlMS" +
            "AvQmFzZUZvbnQgL0hlbHZldGljYSA+PgplbmRvYmoKeHJlZgowIDYKMDAwMDAwMDAwMCA2NTUzNSBmIAowMDAwMDAw" +
            "MDA5IDAwMDAwIG4gCjAwMDAwMDAwNTggMDAwMDAgbiAKMDAwMDAwMDExNSAwMDAwMCBuIAowMDAwMDAwMjQxIDAwMD" +
            "AwIG4gCjAwMDAwMDA0MzQgMDAwMDAgbiAKdHJhaWxlcgo8PCAvU2l6ZSA2IC9Sb290IDEgMCBSID4+CnN0YXJ0eHJl" +
            "Zgo1MDQKJSVFT0YK"
}
