package xyz.wagyourtail.konig.structure.code;

public class InnerCode extends Code {
    public String name;

    public InnerCode(KonigBlockReference block) {
        super(block.parent.parent);
    }

}
