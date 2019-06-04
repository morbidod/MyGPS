package com.diemme.mygps;

import android.support.annotation.NonNull;

import com.rengwuxian.materialedittext.validation.METValidator;

    public class LengthValidator extends METValidator {
        private final int lenght;

        public LengthValidator(@NonNull String errorMessage, @NonNull int lenght) {
            super(errorMessage);
            this.lenght = lenght;
        }

        @Override
        public boolean isValid(@NonNull CharSequence text, boolean isEmpty) {
            return text.length() >= lenght;
        }
    }

