"use strict";

var canvas;
var gl;
var program;

// point array and color array
var pointsArray = [];
var colorsArray = [];

window.onload = function init() {

    canvas = document.getElementById( "gl-canvas" );

    gl = WebGLUtils.setupWebGL( canvas );
    if ( !gl ) { alert( "WebGL isn't available" ); }

    gl.viewport( 0, 0, canvas.width, canvas.height );
    gl.clearColor( 1.0, 1.0, 1.0, 1.0 );

    // add positions and colors of points 
    main();

    // Load shaders and initialize attribute buffers
    program = initShaders( gl, "vertex-shader", "fragment-shader" );
    gl.useProgram( program );

    // push point array and color array in buffers
        //
    //  Load shaders and initialize attribute buffers
    //
    var vBuffer = gl.createBuffer();
    gl.bindBuffer( gl.ARRAY_BUFFER, vBuffer );
    gl.bufferData( gl.ARRAY_BUFFER, flatten(pointsArray), gl.STATIC_DRAW );

    var vPosition = gl.getAttribLocation( program, "vPosition" );
    gl.vertexAttribPointer( vPosition, 2, gl.FLOAT, false, 0, 0 );
    gl.enableVertexAttribArray( vPosition );

    var cBuffer = gl.createBuffer();
    gl.bindBuffer( gl.ARRAY_BUFFER, cBuffer );
    gl.bufferData( gl.ARRAY_BUFFER, flatten(colorsArray), gl.STATIC_DRAW );
    
    var vColor = gl.getAttribLocation( program, "vColor" );
    gl.vertexAttribPointer( vColor, 3, gl.FLOAT, false, 0, 0 );
    gl.enableVertexAttribArray( vColor );

    render();
}

function main() {
    var nx = 500;
    var ny = 500;
   // add your code here:
   var s1 = new sphere(vec3(0,0,-1), 0.5);
   var s2 = new sphere(vec3(0,-100.5,-1), 100);
   var s3 = new sphere(vec3(0.5,0,-1.6), 0.6);
   var world = [s1, s2, s3]; 
   var lower_left_corner = vec3(-1.0, -1.0, -1.0);
   var horiz = vec3(2.0, 0, 0);
   var vert = vec3(0, 2.0, 0);
   var origin = vec3(0,0,0);
   for (var j = ny - 1; j >= 0; j--){
       for ( var i = 0; i < nx; i++){
           var u = i/nx;
           var v = j/ny;
           let r = new ray(origin, add(scale(v, vert) ,add(lower_left_corner, scale(u, horiz))));
           var col = colors(r,world);
           colorsArray.push(col);
           pointsArray.push(vec2(i/250 - 1,j /250-1));
       }
   }
}

function colors(r, world){
    var rec = new hit_record();
    var hit_anything = false; 
    var t_max = Number.MAX_VALUE;
    var t_min = 0; 
    for(var i = 0; i < 3 ; i++){
        if (world[i].hit(r, rec, t_min, t_max)){
            hit_anything = true; 
            t_max = rec.t; 
        }  
    }   
    if(hit_anything){
        return scale(0.5, vec3(rec.normal[0] + 1, rec.normal[1] + 1, rec.normal[2] + 1)); 
    }
    else{
    var unit_dir = normalize(r.direction()); 
    var t = 0.5 * (unit_dir[1] + 1);
    return add(scale((1.0-t), vec3(1.0,1.0,1.0)), scale(t, vec3(0.5, 0.7, 1.0)))
}
}

var render = function() {
    gl.clear( gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
    gl.drawArrays( gl.POINTS, 0, pointsArray.length );
}