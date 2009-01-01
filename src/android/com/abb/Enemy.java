// Copyright 2008 and onwards Matthew Burkhart.
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; version 3 of the License.
//
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// details.

package android.com.abb;

import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;
import java.lang.Math;
import java.util.TreeMap;
import junit.framework.Assert;


public class Enemy extends ArticulatedEntity {
  public Enemy(Entity target) {
    super();
    mTarget = target;
  }

  @Override
  public void step(float time_step) {
    ddy = mGravity;
    super.step(time_step);
    super.stepAnimation(time_step);

    // If we have moved close enough to our target, mark it dead.
    if (Math.abs(mTarget.x - x) < radius && Math.abs(mTarget.y - y) < radius) {
      mTarget.alive = false;
    }

    // If the target has moved far enough away from this entity, destroy it.
    // This may happen if the client leaves an enemy behind on the map. We want
    // to release resources allocated to it.
    if (Math.abs(mTarget.x - x) > kRange || Math.abs(mTarget.y - y) > kRange) {
      alive = false;
    }

    // Always move the enemy towards the target. Set the acceleration and sprite
    // to reflect it.
    int sprite_offset;
    if (mTarget.x < x) {
      sprite_flipped_horizontal = true;
      ddx = -mAcceleration;
    } else {
      sprite_flipped_horizontal = false;
      ddx = mAcceleration;
    }
    if (has_ground_contact) {
      dy = -mJumpVelocity;
    }
  }

  public void loadFromUri(Uri uri) {
    // The following map defines all of the accepted enemy parameters. The
    // enemy_parameters map is expected to populated with default values letting
    // the user override only a subset if desired within the text resource at
    // the specified uri.
    TreeMap<String, Object> enemy_parameters = new TreeMap<String, Object>();
    enemy_parameters.put(kParameterAcceleration, new Float(kDefaultAcceleration));
    enemy_parameters.put(kParameterDrawingScale, new Float(kDefaultDrawingScale));
    enemy_parameters.put(kParameterAnimation, "none");
    enemy_parameters.put(kParameterEntity, "none");
    enemy_parameters.put(kParameterJumpVelocity, new Float(kDefaultJumpVelocity));
    enemy_parameters.put(kParameterGravity, new Float(kDefaultGravity));
    enemy_parameters.put(kParameterLife, new Float(kDefaultLife));
    enemy_parameters.put(kParameterRadius, new Float(kDefaultRadius));

    // Given a fully-specified default enemy parameters map, we can parse and
    // merge in the user defined values. Note that the following method rejects
    // all keys provided by the user which were not defined above.
    String file_path = Content.getTemporaryFilePath(uri);
    String[] tokens = Content.readFileTokens(file_path);
    Content.mergeKeyValueTokensWithMap(tokens, enemy_parameters);

    // Now that the user defined enemy parameters have been parsed and merged,
    // we can initialize the enemy state accordingly.
    mAcceleration = ((Float)enemy_parameters.get(kParameterAcceleration)).floatValue();
    setDrawingScale(((Float)enemy_parameters.get(kParameterDrawingScale)).floatValue());
    mGravity = ((Float)enemy_parameters.get(kParameterGravity)).floatValue();
    mLife = ((Float)enemy_parameters.get(kParameterLife)).floatValue();
    radius = ((Float)enemy_parameters.get(kParameterRadius)).floatValue();

    String uri_string = uri.toString();
    String base_uri_string = uri_string.substring(0, uri_string.lastIndexOf("/"));
    String entity = (String)enemy_parameters.get(kParameterEntity);
    Assert.assertTrue("Enemy entity must be specified.",
                      !entity.equals("none"));
    super.loadFromUri(Uri.parse(base_uri_string + "/" + entity));

    String animation = (String)enemy_parameters.get(kParameterAnimation);
    Assert.assertTrue("Enemy animation must be specified.",
                      !animation.equals("none"));
    super.loadAnimationFromUri(Uri.parse(base_uri_string + "/" + animation));
  }

  private float mAcceleration;
  private float mGravity;
  private float mJumpVelocity;
  private float mLife;
  private Entity mTarget;

  private static final float kDefaultAcceleration = 40.0f;
  private static final float kDefaultDrawingScale = 1.0f;
  private static final float kDefaultJumpVelocity = 100.0f;
  private static final float kDefaultGravity = 100.0f;
  private static final float kDefaultLife = 1.0f;
  private static final float kDefaultRadius = 32.0f;
  private static final String kParameterAcceleration = "acceleration";
  private static final String kParameterAnimation = "animation";
  private static final String kParameterDrawingScale = "drawing_scale";
  private static final String kParameterEntity = "entity";
  private static final String kParameterJumpVelocity = "jump_velocity";
  private static final String kParameterGravity = "gravity";
  private static final String kParameterLife = "life";
  private static final String kParameterRadius = "radius";
  private static final float kRange = 500.0f;
}