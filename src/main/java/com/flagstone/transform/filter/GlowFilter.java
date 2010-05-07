/*
 * GlowFilter.java
 * Transform
 *
 * Copyright (c) 2009-2010 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.flagstone.transform.filter;

import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.Color;

/** TODO(class). */
public final class GlowFilter implements Filter {

    /** TODO(class). */
    public static class Builder {
        private Color color;
        private int blurX;
        private int blurY;
        private int strength;
        private int mode;
        private int passes;
        
        /** TODO(method). */
       public Builder color(Color color) {
            this.color = color;
            return this;
        }
        
        /** TODO(method). */
        public Builder blur(float xAmount, float yAmount) {
            blurX = (int) (xAmount * 65536.0f);
            blurY = (int) (yAmount * 65536.0f);
            return this;
        }
        
        /** TODO(method). */
        public Builder mode(FilterMode mode) {
            switch (mode) {
            case TOP:
                 this.mode = 0x0030;
                break;
            case KNOCKOUT:
                this.mode = 0x0060;
                break;
            case INNER:
                this.mode = 0x00A0;
                break;
            default:
                throw new IllegalArgumentException();
            }
            return this;
        }
        
        /** TODO(method). */
        public Builder strength(float strength) {
            this.strength = (int) (strength * 256.0f);
            return this;
        }
        
        /** TODO(method). */
        public Builder passes(int count) {
            passes = count;
            return this;
        }
        
        /** TODO(method). */
        public GlowFilter build() {
            return new GlowFilter(this);
        }
    }

    private static final String FORMAT = "GlowFilter: { "
            + "color=%s; blurX=%f; blurY=%f; "
            + "strength=%f; mode=%s; passes=%d}";

    private final Color color;
    private final int blurX;
    private final int blurY;
    private final int strength;
    private final int mode;
    private final int passes;

    private GlowFilter(Builder builder) {
        color = builder.color;
        blurX = builder.blurX;
        blurY = builder.blurY;
        strength =  builder.strength;
        mode =  builder.mode;
        passes = builder.passes;
    }

    /**
     * Creates and initialises a GlowFilter object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @param context
     *            a Context object used to manage the decoders for different
     *            type of object and to pass information on how objects are
     *            decoded.
     *
     * @throws CoderException
     *             if an error occurs while decoding the data.
     */
    public GlowFilter(final SWFDecoder coder, final Context context)
            throws CoderException {
        coder.adjustPointer(8);
        color = new Color(coder, context);
        blurX = coder.readWord(4, true);
        blurY = coder.readWord(4, true);
        strength = coder.readWord(2, true);
        
        final int value = coder.readByte();
        
        passes = value & 0x0F;
        mode = (value & 0x0D) >>> 4;
    }

    /** TODO(method). */
    public Color getColor() {
        return color;
    }

    /** TODO(method). */
    public float getBlurX() {
        return blurX / 65536.0f;
    }

    /** TODO(method). */
    public float getBlurY() {
        return blurY / 65536.0f;
    }

    /** TODO(method). */
    public float getStrength() {
        return strength / 256.0f;
    }

    /** TODO(method). */
    public FilterMode getMode() {
        FilterMode value;
        switch (mode) {
        case 0x0060:
            value = FilterMode.KNOCKOUT;
            break;
        case 0x00A0:
            value = FilterMode.INNER;
            break;
        default:
            throw new IllegalStateException();
        }
        return value;
    }

    /** TODO(method). */
    public int getPasses() {
        return passes;
    }

    @Override
    public String toString() {
        return String.format(FORMAT, color.toString(), 
                getBlurX(), getBlurY(), getStrength(), mode, passes);
    }

    @Override
    public boolean equals(final Object object) {
        boolean result;
        GlowFilter filter;

        if (object == null) {
            result = false;
        } else if (object == this) {
            result = true;
        } else if (object instanceof GlowFilter) {
            filter = (GlowFilter) object;
            result = color.equals(filter.color) && (blurX == filter.blurX)
                    && (blurY == filter.blurY) && (strength == filter.strength)
                    && (mode == filter.mode) && (passes == filter.passes);
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return (((((color.hashCode() * 31) + blurX) * 31 + blurY) * 31 
                + strength) * 31 + mode) * 31 + passes;
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final SWFEncoder coder, final Context context) {
        return 16;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws CoderException {
        coder.writeByte(FilterTypes.GLOW);
        color.encode(coder, context);
        coder.writeWord(blurX, 4);
        coder.writeWord(blurY, 4);
        coder.writeWord(strength, 2);
        coder.writeByte(mode | passes);
    }
}