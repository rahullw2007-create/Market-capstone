let token=localStorage.getItem("token")||"";
let currentUser=JSON.parse(localStorage.getItem("user")||"null");

async function api(url,options={}) {
  options.headers=Object.assign({"Content-Type":"application/json"},options.headers||{});
  if(token) options.headers.Authorization="Bearer "+token;
  const r=await fetch(url,options);
  const data=await r.json().catch(()=>({}));
  if(!r.ok) throw new Error(data.error||data.message||"Request failed");
  return data;
}
function refreshUI(){
  document.getElementById("user").textContent=currentUser?`Logged in: ${currentUser.name} (${currentUser.role})`:"Not logged in";
  document.getElementById("seller").classList.toggle("hidden",!(currentUser&&currentUser.role==="seller"));
}
function saveAuth(data){token=data.token;currentUser=data.user;localStorage.setItem("token",token);localStorage.setItem("user",JSON.stringify(currentUser));refreshUI();loadProducts();loadCart();}
document.getElementById("register").onsubmit=async e=>{
 e.preventDefault();try{const d=await api("/api/auth/register",{method:"POST",body:JSON.stringify({name:rname.value,email:remail.value,password:rpass.value,role:rrole.value})});saveAuth(d);alert(d.message)}catch(x){alert(x.message)}
};
document.getElementById("login").onsubmit=async e=>{
 e.preventDefault();try{const d=await api("/api/auth/login",{method:"POST",body:JSON.stringify({email:lemail.value,password:lpass.value})});saveAuth(d);alert(d.message)}catch(x){alert(x.message)}
};
document.getElementById("logout").onclick=()=>{token="";currentUser=null;localStorage.clear();refreshUI();loadCart()};
document.getElementById("productForm").onsubmit=async e=>{
 e.preventDefault();try{await api("/api/products",{method:"POST",body:JSON.stringify({name:pname.value,price:+pprice.value,stock:+pstock.value,category:pcat.value,description:pdesc.value,image_url:pimg.value})});e.target.reset();loadProducts();alert("Product added")}catch(x){alert(x.message)}
};
async function loadProducts(){
 try{const q=new URLSearchParams({search:document.getElementById("search").value,category:document.getElementById("category").value});const d=await api("/api/products?"+q);
 const box=document.getElementById("productList");box.innerHTML="";
 d.products.forEach(p=>{const div=document.createElement("div");div.className="product";div.innerHTML=`<h3>${esc(p.name)}</h3><p>${esc(p.description||"")}</p><p>Category: ${esc(p.category||"")}</p><p>Stock: ${p.stock}</p><p class="price">₹${Number(p.price).toFixed(2)}</p><p>Seller: ${esc(p.seller_name)}</p>${currentUser&&currentUser.role==="buyer"?`<button onclick="addCart(${p.id})">Add to Cart</button>`:""}`;box.appendChild(div)})
 }catch(x){console.error(x)}
}
async function addCart(id){try{await api("/api/cart",{method:"POST",body:JSON.stringify({product_id:id,quantity:1})});loadCart();alert("Added to cart")}catch(x){alert(x.message)}}
async function loadCart(){
 const box=document.getElementById("cartList");box.innerHTML="";
 if(!currentUser||currentUser.role!=="buyer"){box.textContent="Login as a buyer to use the cart.";return}
 try{const d=await api("/api/cart");d.items.forEach(i=>{box.innerHTML+=`<div class="cartrow"><span>${esc(i.name)} × ${i.quantity}</span><span>₹${Number(i.subtotal).toFixed(2)}</span></div>`});box.innerHTML+=`<h3>Total: ₹${Number(d.total).toFixed(2)}</h3>`}catch(x){box.textContent=x.message}
}
document.getElementById("checkout").onclick=async()=>{try{const d=await api("/api/orders",{method:"POST",body:"{}"});document.getElementById("orderMsg").textContent=`Order #${d.order_id} placed. Mock payment: ${d.payment_status}. Total ₹${Number(d.total).toFixed(2)}`;loadCart();loadProducts()}catch(x){alert(x.message)}};
function esc(s){return String(s).replace(/[&<>"']/g,m=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"}[m]))}
refreshUI();loadProducts();loadCart();
