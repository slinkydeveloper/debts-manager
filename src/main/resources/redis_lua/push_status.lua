local username = KEYS[1]
local command_id = KEYS[2]
redis.call("del", "commands:" .. username)
redis.call("del", "status:" .. username)
redis.call("sadd", "commands:" .. username, command_id)
redis.call("hmset", "status:" .. username, unpack(ARGV))
return 1
