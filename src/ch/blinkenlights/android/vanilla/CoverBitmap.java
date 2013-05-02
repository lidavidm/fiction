/*
 * Copyright (C) 2010, 2011 Christopher Eby <kreed@kreed.org>
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

package ch.blinkenlights.android.vanilla;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Class containing utility functions to create Bitmaps displaying album
 * art.
 */
public final class CoverBitmap {
	/**
	 * Create an image representing the given song. Includes cover art and
	 * possibly song title/artist/ablum, depending on the given style.
	 *
	 * @param context A context to use.
	 * @param coverArt The cover art for the song.
	 * @param width Maximum width of image
	 * @param height Maximum height of image
	 * @return The image, or null if the song was null, or width or height
	 * were less than 1
	 */
	public static Bitmap createBitmap(Context context, Bitmap coverArt, int width, int height)
	{
        return createScaledBitmap(coverArt, width, height);
	}

	/**
	 * Scales a bitmap to fit in a rectangle of the given size. Aspect ratio is
	 * preserved. At least one dimension of the result will match the provided
	 * dimension exactly.
	 *
	 * @param source The bitmap to be scaled
	 * @param width Maximum width of image
	 * @param height Maximum height of image
	 * @return The scaled bitmap.
	 */
	private static Bitmap createScaledBitmap(Bitmap source, int width, int height)
	{
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		float scale = Math.min((float)width / sourceWidth, (float)height / sourceHeight);
		sourceWidth *= scale;
		sourceHeight *= scale;
		return Bitmap.createScaledBitmap(source, sourceWidth, sourceHeight, false);
	}

	/**
	 * Generate the default cover (a rendition of a CD). Returns a square iamge.
	 * Both dimensions are the lesser of width and height.
	 *
	 * @param width The max width
	 * @param height The max height
	 * @return The default cover.
	 */
	public static Bitmap generateDefaultCover(int width, int height)
	{
		int size = Math.min(width, height);
		int halfSize = size / 2;
		int eightSize = size / 8;

		Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
		LinearGradient gradient = new LinearGradient(size, 0, 0, size, 0xff646464, 0xff464646, Shader.TileMode.CLAMP);
		RectF oval = new RectF(eightSize, 0, size - eightSize, size);

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		Canvas canvas = new Canvas(bitmap);
		canvas.rotate(-45, halfSize, halfSize);

		paint.setShader(gradient);
		canvas.translate(size / 20, size / 20);
		canvas.scale(0.9f, 0.9f);
		canvas.drawOval(oval, paint);

		paint.setShader(null);
		paint.setColor(0xff000000);
		canvas.translate(size / 3, size / 3);
		canvas.scale(0.333f, 0.333f);
		canvas.drawOval(oval, paint);

		paint.setShader(gradient);
		canvas.translate(size / 3, size / 3);
		canvas.scale(0.333f, 0.333f);
		canvas.drawOval(oval, paint);

		return bitmap;
	}
}
