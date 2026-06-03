const apiBase = `${window.location.protocol}//${window.location.hostname}:9000`;
let session = JSON.parse(localStorage.getItem("leaveSession") || "null");

const qs = (id) => document.getElementById(id);

function setMessage(text) {
  qs("message").textContent = text || "";
}

function authHeaders() {
  return {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${session.token}`
  };
}

async function request(path, options = {}) {
  const response = await fetch(`${apiBase}${path}`, options);
  if (!response.ok) {
    const body = await response.json().catch(() => ({ message: response.statusText }));
    throw new Error(body.message || response.statusText);
  }
  return response.status === 204 ? null : response.json();
}

function showApp() {
  qs("loginPanel").classList.toggle("hidden", !!session);
  qs("appPanel").classList.toggle("hidden", !session);
  qs("logoutBtn").classList.toggle("hidden", !session);
  qs("userLine").textContent = session ? `${session.name} (${session.role})` : "Login to start";
}

async function loadEmployeeData() {
  if (!session || session.role !== "EMPLOYEE") return;
  const balances = await request(`/employees/${session.userId}/leave-balances`, { headers: authHeaders() });
  qs("balances").innerHTML = balances.map(item => `
    <div class="item">
      <strong>${item.leaveType}</strong>
      Total: ${item.totalAllocated} | Used: ${item.usedLeaves} | Remaining: ${item.remainingLeaves}
    </div>`).join("");

  const history = await request("/leaves/history?page=0&size=20", { headers: authHeaders() });
  qs("history").innerHTML = (history.content || []).map(renderLeave).join("") || "<p>No leave requests yet.</p>";
}

function renderLeave(item) {
  return `
    <div class="item">
      <strong>#${item.id} ${item.leaveType} - ${item.status}</strong>
      ${item.startDate} to ${item.endDate}, ${item.numberOfDays} day(s)<br>
      Employee: ${item.employeeId}, Manager: ${item.managerId}<br>
      Reason: ${item.reason || "-"}${item.rejectionReason ? `<br>Rejection: ${item.rejectionReason}` : ""}
    </div>`;
}

async function loadManagerData() {
  if (!session || session.role !== "MANAGER") {
    qs("managerRequests").innerHTML = "<p>Login as a manager to use this view.</p>";
    return;
  }
  const leaves = await request("/manager/leaves", { headers: authHeaders() });
  qs("managerRequests").innerHTML = leaves.map(item => `
    <div class="item">
      <strong>#${item.id} ${item.leaveType} - ${item.status}</strong>
      Employee ${item.employeeId}: ${item.startDate} to ${item.endDate}, ${item.numberOfDays} day(s)<br>
      ${item.reason || ""}
      ${item.status === "PENDING" ? `
        <div class="actions">
          <button onclick="approveLeave(${item.id})">Approve</button>
          <button class="secondary" onclick="rejectLeave(${item.id})">Reject</button>
        </div>` : ""}
    </div>`).join("") || "<p>No requests found.</p>";
}

async function loadNotifications() {
  if (!session) return;
  const data = await request("/notifications/me", { headers: authHeaders() });
  qs("notifications").innerHTML = data.map(item => `
    <div class="item">
      <strong>${item.eventType}</strong>
      ${item.message}<br>
      Leave #${item.leaveId} | ${item.createdAt}
    </div>`).join("") || "<p>No notifications yet.</p>";
}

window.approveLeave = async (id) => {
  try {
    await request(`/manager/leaves/${id}/approve`, { method: "POST", headers: authHeaders() });
    setMessage("Leave approved.");
    await loadManagerData();
  } catch (error) {
    setMessage(error.message);
  }
};

window.rejectLeave = async (id) => {
  const reason = prompt("Rejection reason", "Not enough coverage");
  if (!reason) return;
  try {
    await request(`/manager/leaves/${id}/reject`, {
      method: "POST",
      headers: authHeaders(),
      body: JSON.stringify({ reason })
    });
    setMessage("Leave rejected.");
    await loadManagerData();
  } catch (error) {
    setMessage(error.message);
  }
};

qs("loginForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    session = await request("/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: qs("email").value, password: qs("password").value })
    });
    localStorage.setItem("leaveSession", JSON.stringify(session));
    showApp();
    setMessage("Logged in.");
    await loadEmployeeData();
  } catch (error) {
    setMessage(error.message);
  }
});

qs("applyForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    await request("/leaves", {
      method: "POST",
      headers: authHeaders(),
      body: JSON.stringify({
        leaveType: qs("leaveType").value,
        startDate: qs("startDate").value,
        endDate: qs("endDate").value,
        numberOfDays: Number(qs("days").value),
        reason: qs("reason").value,
        managerId: Number(qs("managerId").value)
      })
    });
    setMessage("Leave submitted.");
    await loadEmployeeData();
  } catch (error) {
    setMessage(error.message);
  }
});

document.querySelectorAll("[data-tab]").forEach(button => {
  button.addEventListener("click", async () => {
    document.querySelectorAll("[data-tab]").forEach(item => item.classList.remove("active"));
    button.classList.add("active");
    document.querySelectorAll(".tab-panel").forEach(panel => panel.classList.add("hidden"));
    qs(`${button.dataset.tab}Tab`).classList.remove("hidden");
    if (button.dataset.tab === "manager") await loadManagerData();
    if (button.dataset.tab === "notifications") await loadNotifications();
  });
});

qs("refreshManager").addEventListener("click", loadManagerData);
qs("refreshNotifications").addEventListener("click", loadNotifications);
qs("logoutBtn").addEventListener("click", () => {
  session = null;
  localStorage.removeItem("leaveSession");
  showApp();
});

showApp();
loadEmployeeData().catch(error => setMessage(error.message));
