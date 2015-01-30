// GLOBAL SCOPE: a, f
var a;

// FUNCTION SCOPE: f, p, a, b
function f (p) {
  var a;

  // FUNCTION SCOPE: x
  var b = function g() {
    var x;
  }
}
