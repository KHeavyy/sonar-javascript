/**
 * GLOBAL SCOPE: a, b, f
 */
var a;
b = 1; // implicit declaration - global scope

/**
 *  FUNCTION SCOPE: p, a, b
 */
function f (p) {
  var a;
  c = 1;  // implicit declaration - global scope

  /**
   *  FUNCTION SCOPE: x
   */
  var b = function g() {
    var x;
    var a;
    var x;
  }

}
