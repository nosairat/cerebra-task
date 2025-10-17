package sa.cerebra.task.helper;

import lombok.RequiredArgsConstructor;
import sa.cerebra.task.entity.User;

import java.nio.file.Paths;

@RequiredArgsConstructor
public class PathHelper {
    public static String getUserStoragePath(User user, String relativeUserPath) {
        String userStoragePath = getUserStoragePath(user);
        return getActualPath(userStoragePath, relativeUserPath);
    }

    private static String getUserStoragePath(User user) {
        return user.getId().toString();
    }

    private static String getActualPath(String userStoragePath, String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return Paths.get(userStoragePath).normalize().toString();
        }
        if (Paths.get(relativePath).isAbsolute())
            return getActualPath(userStoragePath, relativePath.replaceFirst("/", ""));

        return Paths.get(userStoragePath).resolve(relativePath).normalize().toString();
    }
}
