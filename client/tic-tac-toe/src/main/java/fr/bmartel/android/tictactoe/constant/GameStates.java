/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016 Bertrand Martel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.android.tictactoe.constant;

/**
 * @author Bertrand Martel
 */
public enum GameStates {

    NONE(0),
    CONNECTED(1),
    CHALLENGED(2),
    GAME_STARTING(3),
    PLAYING(4),
    GAME_ENDED(5);

    private int value = 0;

    private GameStates(int value) {
        this.value = value;
    }

    public static GameStates getState(int value) {

        switch (value) {
            case 0:
                return NONE;
            case 1:
                return CONNECTED;
            case 2:
                return CHALLENGED;
            case 3:
                return GAME_STARTING;
            case 4:
                return PLAYING;
            case 5:
                return GAME_ENDED;
        }
        return NONE;
    }
}
