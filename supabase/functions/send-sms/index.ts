import { Webhook } from "https://esm.sh/standardwebhooks@1.0.0";

Deno.serve(async (req: Request) => {
  if (req.method !== "POST") return new Response("Method not allowed", { status: 405 });

  const hookSecret = Deno.env.get("SEND_SMS_HOOK_SECRET");
  const birdApiKey = Deno.env.get("BIRD_API_KEY");
  const birdBaseUrl = (Deno.env.get("BIRD_API_BASE_URL") || "https://eu1.platform.bird.com").replace(/\/$/, "");

  if (!hookSecret || !birdApiKey) {
    console.error("Missing SEND_SMS_HOOK_SECRET or BIRD_API_KEY");
    return new Response("SMS provider is not configured", { status: 500 });
  }

  const rawBody = await req.text();
  try {
    const webhook = new Webhook(hookSecret.replace(/^v1,whsec_/, ""));
    webhook.verify(rawBody, Object.fromEntries(req.headers.entries()));
  } catch {
    return new Response("Invalid webhook signature", { status: 401 });
  }

  let payload: { user?: { phone?: string }; sms?: { otp?: string } };
  try {
    payload = JSON.parse(rawBody);
  } catch {
    return new Response("Invalid JSON", { status: 400 });
  }

  const phone = payload.user?.phone;
  const otp = payload.sms?.otp;
  if (!phone || !/^\+[1-9]\d{7,14}$/.test(phone) || !otp || !/^\d{4,10}$/.test(otp)) {
    return new Response("Invalid phone or OTP payload", { status: 400 });
  }

  const response = await fetch(`${birdBaseUrl}/v1/sms/messages`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${birdApiKey}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      to: phone,
      text: `NULL Messenger: ваш код подтверждения ${otp}. Никому не сообщайте его.`,
      category: "authentication",
    }),
  });

  if (!response.ok) {
    console.error("Bird SMS request failed", response.status, (await response.text()).slice(0, 500));
    return new Response("SMS delivery provider rejected the request", { status: 502 });
  }

  return new Response(null, { status: 200 });
});
