local command_id = KEYS[2]
local username = KEYS[1]
local username_to = KEYS[3]
local value = KEYS[4]
if redis.call("sismember", "commands:" .. username, command_id) == 0 and redis.call("exists", "commands:" .. username) == 1 then
  redis.call("sadd", "commands:" .. username, command_id)
  redis.call("hincrbyfloat", "status:" .. username, username_to, value)
  return 1
else
  return 0
end
