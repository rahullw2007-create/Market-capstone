let currentRole = null;

document.getElementById('register').onsubmit = function(e) {
  e.preventDefault();

  currentRole = document.getElementById('rrole').value;

  alert('Registered as ' + currentRole);

  if(currentRole === 'seller') {
    document.getElementById('seller').classList.remove('hidden');
  } else {
    document.getElementById('seller').classList.add('hidden');
  }
};

document.getElementById('login').onsubmit = function(e) {
  e.preventDefault();

  alert('Login successful');

  if(currentRole === 'seller') {
    document.getElementById('seller').classList.remove('hidden');
  } else {
    document.getElementById('seller').classList.add('hidden');
  }
};

document.getElementById('logout').onclick = function() {
  currentRole = null;
  document.getElementById('seller').classList.add('hidden');
  alert('Logged out');
};

document.getElementById('productForm').onsubmit = function(e) {
  e.preventDefault();

  if(currentRole !== 'seller') {
    alert('Only sellers can add products');
    return;
  }

  const name = document.getElementById('pname').value;
  const price = document.getElementById('pprice').value;

  const box = document.getElementById('productList');

  box.innerHTML += `
    <div class="product">
      <h3>${name}</h3>
      <p class="price">₹${price}</p>
      <button onclick="addToCart('${name}', ${price})">Add to Cart</button>
    </div>
  `;

  alert('Product added successfully');
};

function addToCart(name, price) {
  const cart = document.getElementById('cartList');

  cart.innerHTML += `
    <div class="cartrow">
      <span>${name}</span>
      <span>₹${price}</span>
    </div>
  `;

  alert('Added to cart');
}

document.getElementById('checkout').onclick = function() {
  document.getElementById('orderMsg').textContent =
    'Order placed successfully. Mock payment: SUCCESS';
};

// Hide seller section initially
document.getElementById('seller').classList.add('hidden');