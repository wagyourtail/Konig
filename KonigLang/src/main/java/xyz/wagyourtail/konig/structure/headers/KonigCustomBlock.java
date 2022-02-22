package xyz.wagyourtail.konig.structure.headers;

import org.w3c.dom.Node;
import xyz.wagyourtail.konig.structure.code.Code;

import java.io.IOException;

public class KonigCustomBlock extends KonigBlock {
    public final KonigHeaders parent;
    public final Code code;
    public final BlockIO innerio = new BlockIO();

    public KonigCustomBlock(KonigHeaders parent) {
        this.parent = parent;
        this.code = new Code(parent);
    }

    @Override
    protected boolean parseChild(Node child) throws IOException {
        if (child.getNodeName().equals("code")) {
            code.parseXML(child);
            return true;
        }
        if (child.getNodeName().equals("innerio")) {
            innerio.parseXML(child);
        }
        return super.parseChild(child);
    }

}
